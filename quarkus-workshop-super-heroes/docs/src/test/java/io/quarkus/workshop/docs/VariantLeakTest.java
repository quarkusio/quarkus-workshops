package io.quarkus.workshop.docs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests that variant-specific content doesn't leak into variants where that module is disabled.
 * Prevents regressions like issue #749 where Kafka/event-statistics appeared in messaging-off variants.
 * Configuration is centralized in variant-leak-config.yaml for easy maintenance.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VariantLeakTest {

    private static final File VARIANTS_PATH = new File(
        System.getProperty("docs.base.path", "target/generated-asciidoc/"), "variants");

    static class VariantConfig {
        String filterPattern;
        List<BannedTerm> bannedTerms;

        static class BannedTerm {
            String term;
            boolean caseInsensitive;
            List<String> allowedPhrases;
        }
    }

    @ParameterizedTest
    @MethodSource("loadVariantConfigs")
    @DisplayName("Variant should not leak disabled module content")
    @Order(1)
    void variantsShouldNotLeak(VariantConfig config) throws IOException {
        List<Path> variants = getVariants(config.filterPattern);
        assumeTrue(!variants.isEmpty(), "Need at least one " + config.filterPattern + " variant to test");

        for (Path variant : variants) {
            String content = Files.readString(variant);
            String variantName = variant.getParent().getFileName().toString();

            for (VariantConfig.BannedTerm bannedTerm : config.bannedTerms) {
                assertDoesNotContain(content, variantName, bannedTerm.term,
                    bannedTerm.allowedPhrases);
            }
        }
    }

    static List<VariantConfig> loadVariantConfigs() throws IOException {
        List<VariantConfig> configs = new ArrayList<>();

        String configPath = System.getProperty("variants.config.path",
            "src/resource-generation/variants-config.json");
        Path jsonPath = Files.exists(Path.of(configPath)) ? Path.of(configPath):null;

        if (jsonPath==null) {
            // Fallback: if property not set, search for it
            jsonPath = Files.walk(Path.of("."))
                .filter(p -> p.getFileName().toString().equals("variants-config.json"))
                .findFirst()
                .orElseThrow(() -> new IOException("variants-config.json not found"));
        }

        try (JsonReader reader = Json.createReader(Files.newInputStream(jsonPath))) {
            JsonObject root = reader.readObject();
            JsonObject leakConfig = root.getJsonObject("leakTestConfig");

            for (String variantKey : leakConfig.keySet()) {
                JsonObject variantData = leakConfig.getJsonObject(variantKey);
                VariantConfig config = new VariantConfig();
                config.filterPattern = variantData.getString("filterPattern");
                config.bannedTerms = new ArrayList<>();

                variantData.getJsonArray("bannedTerms").forEach(item -> {
                    JsonObject termObj = (JsonObject) item;
                    VariantConfig.BannedTerm term = new VariantConfig.BannedTerm();
                    term.term = termObj.getString("term");
                    term.caseInsensitive = termObj.getBoolean("caseInsensitive");
                    term.allowedPhrases = new ArrayList<>();
                    termObj.getJsonArray("allowedPhrases").stream()
                        .map(v -> v.toString().replaceAll("^\"|\"$", ""))
                        .forEach(term.allowedPhrases::add);
                    config.bannedTerms.add(term);
                });

                configs.add(config);
            }
        }

        return configs;
    }

    // Helper methods

    private List<Path> getVariants(String filterPattern) throws IOException {
        Path variantsDir = VARIANTS_PATH.toPath();
        assumeTrue(Files.exists(variantsDir), "Variants directory not generated yet");

        List<Path> variants = new ArrayList<>();
        try (Stream<Path> paths = Files.list(variantsDir)) {
            paths.filter(Files::isDirectory)
                .filter(dir -> dir.getFileName().toString().contains(filterPattern))
                .forEach(dir -> {
                    Path spineFile = dir.resolve("spine.html");
                    if (Files.exists(spineFile)) {
                        variants.add(spineFile);
                    }
                });
        }
        return variants;
    }

    private void assertDoesNotContain(String content, String variantName, String term,
                                      List<String> allowedPhrases) {
        String searchContent = content.toLowerCase();
        String searchTerm = term.toLowerCase();

        int index = searchContent.indexOf(searchTerm);
        if (index >= 0) {
            String contextStr = extractContext(content, index, searchTerm.length());

            // Check if this match is allowed by any of the allowed phrases
            if (isAllowedByPattern(contextStr, allowedPhrases)) {
                return;
            }

            String message = String.format(
                "Variant '%s' contains '%s' but that module is disabled.%nContext: %s",
                variantName, term, contextStr);
            fail(message);
        }
    }

    private boolean isAllowedByPattern(String context, List<String> allowedPhrases) {
        if (allowedPhrases==null || allowedPhrases.isEmpty()) {
            return false;
        }

        for (String pattern : allowedPhrases) {
            try {
                // TODO pull out this compilation
                int flags = Pattern.CASE_INSENSITIVE;
                if (Pattern.compile(pattern, flags).matcher(context).find()) {
                    return true;
                }
            } catch (Exception e) {
                // Log malformed regex patterns but don't fail the test
                System.err.println(e.getMessage());
            }
        }
        return false;
    }

    private String extractContext(String content, int termIndex, int termLength) {
        int contextStart = Math.max(0, termIndex - 400);
        int contextEnd = Math.min(content.length(), termIndex + termLength + 400);
        String context = content.substring(contextStart, contextEnd);

        // Remove any leading/trailing HTML tags or partial words
        context = context.replaceAll("^[^.!?]*", "").replaceAll("[^.!?]*$", "");
        return " " + context.trim();
    }
}
