package io.quarkus.workshop.docs;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Tests for documentation variants.
 * These tests can only be run after variants are generated, which is time-consuming.
 * Tests are skipped when the variants directory does not exist.
 */
public class VariantsTest extends DocumentationTestBase {

    private static final File VARIANTS_PATH = new File(DOCS_BASE_PATH,"variants");

    @BeforeAll
    static void checkVariantsExist() {
        assumeTrue(VARIANTS_PATH.exists(),
            "Variants directory does not exist: " + VARIANTS_PATH + ". Skipping variant tests.");
    }

    static Stream<Path> variantProvider() throws IOException {
        Path variantsDir = VARIANTS_PATH.toPath();

        assumeTrue(Files.exists(variantsDir),
            String.format("The variants directory, %s, does not exist.", variantsDir));

        List<Path> variants = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(variantsDir, 2)) {
            paths.filter(Files::isDirectory)
                 .filter(p -> !p.equals(variantsDir))
                 .forEach(dir -> {
                     Path spineFile = dir.resolve(SPINE_HTML);
                     if (Files.exists(spineFile)) {
                         variants.add(spineFile);
                     }
                 });
        }

        return variants.stream();
    }

    @ParameterizedTest
    @MethodSource("variantProvider")
    @DisplayName("Each variant should load without errors")
    void testVariantLoadsSuccessfully(Path variantFile) {
        String fileUrl = "file://" + variantFile.toAbsolutePath();

        Response response = page.navigate(fileUrl);
        assertNotNull(response, "Variant page should load: " + variantFile);
        assertTrue(response.ok() || response.status() == 0,
            "Variant page should load successfully: " + variantFile + " (status: " + response.status() + ")");
    }

    @ParameterizedTest
    @MethodSource("variantProvider")
    @DisplayName("Each variant should have proper title")
    void testVariantHasTitle(Path variantFile) {
        navigateTo(variantFile);

        String title = page.title();
        assertNotNull(title, "Variant should have a title: " + variantFile);
        assertFalse(title.isEmpty(), "Variant title should not be empty: " + variantFile);
        assertTrue(title.toLowerCase().contains("quarkus") || title.toLowerCase().contains("workshop"),
            "Variant title should contain 'Quarkus' or 'Workshop': " + variantFile + ", but was: " + title);
    }

    @ParameterizedTest
    @MethodSource("variantProvider")
    @DisplayName("Each variant should have table of contents")
    void testVariantHasTableOfContents(Path variantFile) {
        navigateTo(variantFile);

        Locator toc = page.locator("#toc, .toc, nav");
        assertTrue(toc.count() > 0,
            "Variant should have a table of contents: " + variantFile);
    }

    @ParameterizedTest
    @MethodSource("variantProvider")
    @DisplayName("Each variant should have main content sections")
    void testVariantHasMainSections(Path variantFile) {
        navigateTo(variantFile);

        int h2Count = page.locator("h2").count();
        assertTrue(h2Count >= 5,
            "Variant " + variantFile.getFileName() + " should have at least 5 top-level sections, but found " + h2Count);

        int sectionCount = page.locator(".sect1").count();
        assertTrue(sectionCount >= 5,
            "Variant " + variantFile.getFileName() + " should have at least 5 sect1 blocks, but found " + sectionCount);
    }

    @ParameterizedTest
    @MethodSource("variantProvider")
    @DisplayName("Each variant internal links should work")
    void testVariantInternalLinksWork(Path variantFile) {
        navigateTo(variantFile);

        Set<String> brokenLinks = findBrokenInternalLinks();

        assertTrue(brokenLinks.isEmpty(),
            "Variant " + variantFile.getFileName() + " has broken internal links: " +
            String.join(", ", brokenLinks));
    }

    @ParameterizedTest
    @MethodSource("variantProvider")
    @DisplayName("Each variant should not have broken image references")
    void testVariantImagesLoad(Path variantFile) {
        navigateTo(variantFile);

        Set<String> brokenImages = findBrokenImages();

        assertTrue(brokenImages.isEmpty(),
            "Variant " + variantFile.getFileName() + " has broken images: " +
            String.join(", ", brokenImages));
    }

    @ParameterizedTest
    @MethodSource("variantProvider")
    @DisplayName("Each variant should have code blocks")
    void testVariantHasCodeBlocks(Path variantFile) {
        navigateTo(variantFile);

        Locator codeBlocks = page.locator("pre code, .listingblock, .code");
        assertTrue(codeBlocks.count() > 0,
            "Variant should have code blocks: " + variantFile);
    }

    @Test
    @DisplayName("Sample of variants should have different content based on flags")
    void testVariantsHaveDifferentContent() throws IOException {
        Path variantsDir = VARIANTS_PATH.toPath();
        assumeTrue(Files.exists(variantsDir), "Variants directory not generated yet");

        List<Path> variantFiles = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(variantsDir, 2)) {
            paths.filter(Files::isDirectory)
                 .filter(p -> !p.equals(variantsDir))
                 .limit(5)
                 .forEach(dir -> {
                     Path spineFile = dir.resolve(SPINE_HTML);
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
