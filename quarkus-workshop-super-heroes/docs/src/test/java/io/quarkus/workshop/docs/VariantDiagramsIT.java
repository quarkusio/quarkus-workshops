package io.quarkus.workshop.docs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Checks that every variant's diagrams were rendered for that variant's flags.
 * <p>
 * The variants are all generated in one Roq run, so this is also what catches two variants
 * writing their renders of a diagram to the same file, where whichever was rendered last wins
 * for all of them. The canonical site cannot show that; see
 * {@link DocumentationTest#testSpineDiagramsMatchDefaultVariant()} for its own check.
 */
public class VariantDiagramsIT extends BrowserIT {

    private static final Path VARIANTS = new File(DOCS_BASE_PATH, "variants").toPath();

    @TestFactory
    Stream<DynamicTest> everyVariantShouldHaveItsOwnDiagrams() throws IOException {
        assumeTrue(Files.isDirectory(VARIANTS), "Variants directory not generated yet");
        Path siteRoot = DOCS_BASE_PATH.toPath();
        List<Path> variants;
        try (Stream<Path> dirs = Files.list(VARIANTS)) {
            variants = dirs.filter(dir -> Files.exists(dir.resolve(SPINE_HTML))).sorted().toList();
        }
        return variants.stream()
            .map(dir -> DynamicTest.dynamicTest(dir.getFileName().toString(), () -> {
                List<String> problems = Diagrams.problems(dir.resolve(SPINE_HTML), siteRoot, SITE_PATH,
                    flagsOf(dir.getFileName().toString()));
                assertTrue(problems.isEmpty(), String.join("\n", problems));
            }));
    }

    // A variant directory is named bt-<tool>-os-<os>-<flag>-<true|false>-..., by writeVariant in
    // generate-variants.java.
    private static Map<String, Boolean> flagsOf(String variantName) {
        Map<String, Boolean> flags = new LinkedHashMap<>();
        for (VariantsConfig.Flag flag : VariantsConfig.load().flags()) {
            Matcher m = Pattern.compile("-" + Pattern.quote(flag.id()) + "-(true|false)(-|$)").matcher(variantName);
            if (!m.find()) {
                throw new IllegalArgumentException("No value for " + flag.id() + " in variant " + variantName);
            }
            flags.put(flag.id(), Boolean.parseBoolean(m.group(1)));
        }
        return flags;
    }
}
