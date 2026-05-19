package io.quarkus.workshop.docs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for the variant spine.htmls that don't rely on Playwright.
 */
public class VariantsSenseTest {

    private static final File VARIANTS_PATH = new File(
        System.getProperty("docs.base.path", "target/generated-asciidoc/"), "variants");

    @Test
    @DisplayName("Sample of variants should have different content based on flags")
    void variantsShouldHaveDifferentContent() throws IOException {
        Path variantsDir = VARIANTS_PATH.toPath();
        assumeTrue(Files.exists(variantsDir), "Variants directory not generated yet");

        List<Path> variantFiles = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(variantsDir, 2)) {
            paths.filter(Files::isDirectory)
                 .filter(p -> !p.equals(variantsDir))
                 .limit(5)
                 .forEach(dir -> {
                     Path spineFile = dir.resolve("spine.html");
                     if (Files.exists(spineFile)) {
                         variantFiles.add(spineFile);
                     }
                 });
        }

        assumeTrue(variantFiles.size() >= 2, "Need at least 2 variants to compare");

        Set<Long> fileSizes = new HashSet<>();
        for (Path variant : variantFiles) {
            fileSizes.add(Files.size(variant));
        }

        if (variantFiles.size() > 2) {
            assertTrue(fileSizes.size() > 1,
                "Variants should have different content sizes (found " + fileSizes.size() +
                " unique sizes from " + variantFiles.size() + " variants)");
        }
    }
}
