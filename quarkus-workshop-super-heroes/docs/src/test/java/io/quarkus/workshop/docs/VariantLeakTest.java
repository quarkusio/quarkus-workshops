package io.quarkus.workshop.docs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests that variant-specific content doesn't leak into variants where that module is disabled.
 * Prevents regressions like issue #749 where Kafka/event-statistics appeared in messaging-off variants.
 */
public class VariantLeakTest {

    private static final File VARIANTS_PATH = new File(
        System.getProperty("docs.base.path", "target/generated-asciidoc/"), "variants");

    @Test
    @DisplayName("Messaging-off variants should not contain Kafka or event-statistics references")
    void messagingOffVariantsShouldNotLeak() throws IOException {
        List<Path> variants = getVariants("messaging-false");
        assumeTrue(!variants.isEmpty(), "Need at least one messaging-off variant to test");

        for (Path variant : variants) {
            String content = Files.readString(variant);
            String variantName = variant.getParent().getFileName().toString();

            assertDoesNotContain(content, variantName, "kafka", true);
            assertDoesNotContain(content, variantName, "event-statistics", false);
            assertDoesNotContain(content, variantName, "Reactive Messaging", false);
        }
    }

    @Test
    @DisplayName("Messaging-off variants should not contain messaging package or property references")
    void messagingOffVariantsShouldNotLeakPackages() throws IOException {
        List<Path> variants = getVariants("messaging-false");
        assumeTrue(!variants.isEmpty(), "Need at least one messaging-off variant to test");

        for (Path variant : variants) {
            String content = Files.readString(variant);
            String variantName = variant.getParent().getFileName().toString();

            assertDoesNotContain(content, variantName, "reactive.messaging", false);
            assertDoesNotContain(content, variantName, "mp.messaging", false);
        }
    }

    @Test
    @DisplayName("Azure-off variants should not contain Azure or Container Apps references")
    void azureOffVariantsShouldNotLeak() throws IOException {
        List<Path> variants = getVariants("azure-false");
        assumeTrue(!variants.isEmpty(), "Need at least one azure-off variant to test");

        for (Path variant : variants) {
            String content = Files.readString(variant);
            String variantName = variant.getParent().getFileName().toString();

            assertDoesNotContain(content, variantName, "azure", true);
            assertDoesNotContain(content, variantName, "container apps", true);
        }
    }

    @Test
    @DisplayName("Kubernetes-off variants should not contain Kubernetes, kubectl, or k8s references")
    void kubernetesOffVariantsShouldNotLeak() throws IOException {
        List<Path> variants = getVariants("kubernetes-false");
        assumeTrue(!variants.isEmpty(), "Need at least one kubernetes-off variant to test");

        for (Path variant : variants) {
            String content = Files.readString(variant);
            String variantName = variant.getParent().getFileName().toString();

            assertDoesNotContain(content, variantName, "kubernetes", true);
            assertDoesNotContain(content, variantName, "kubectl", true);
            assertDoesNotContain(content, variantName, "k8s", true);
        }
    }

    @Test
    @DisplayName("AI-off variants should not contain OpenAI, LangChain, or narration references")
    void aiOffVariantsShouldNotLeak() throws IOException {
        List<Path> variants = getVariants("ai-false");
        assumeTrue(!variants.isEmpty(), "Need at least one ai-off variant to test");

        for (Path variant : variants) {
            String content = Files.readString(variant);
            String variantName = variant.getParent().getFileName().toString();

            assertDoesNotContain(content, variantName, "openai", true);
            assertDoesNotContain(content, variantName, "open ai", true);
            assertDoesNotContain(content, variantName, "langchain", true);
            assertDoesNotContain(content, variantName, "rest-narration", true);
            assertDoesNotContain(content, variantName, "narration microservice", true);
        }
    }

    @Test
    @DisplayName("Native-off variants should not contain GraalVM or native-image references")
    void nativeOffVariantsShouldNotLeak() throws IOException {
        List<Path> variants = getVariants("native-false");
        assumeTrue(!variants.isEmpty(), "Need at least one native-off variant to test");

        for (Path variant : variants) {
            String content = Files.readString(variant);
            String variantName = variant.getParent().getFileName().toString();

            // Note: we allow "native" as it's too common, but check for specific terms
            assertDoesNotContain(content, variantName, "graalvm", true);
            assertDoesNotContain(content, variantName, "native-image", true);
            assertDoesNotContain(content, variantName, "native image", true);
        }
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

    private void assertDoesNotContain(String content, String variantName, String term, boolean caseInsensitive) {
        String searchContent = caseInsensitive ? content.toLowerCase():content;
        String searchTerm = caseInsensitive ? term.toLowerCase():term;

        boolean contains = searchContent.contains(searchTerm);
        assertFalse(contains,
            "Variant '" + variantName + "' contains '" + term + "' but that module is disabled");
    }
}
