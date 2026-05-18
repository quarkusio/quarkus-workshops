package io.quarkus.workshop.docs;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

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
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Tests for documentation variants.
 * These tests can only be run after variants are generated, which is time-consuming.
 * Tests are skipped when the variants directory does not exist.
 */
public class VariantsTest extends DocumentationTestBase {

    private static final File VARIANTS_PATH = new File(DOCS_BASE_PATH, "variants");

    @BeforeAll
    static void checkVariantsExist() {
        assumeTrue(VARIANTS_PATH.exists(),
            "Variants directory does not exist: " + VARIANTS_PATH + ". Skipping variant tests.");
    }

    private static List<Path> findVariants() throws IOException {
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

        return variants;
    }

    @TestFactory
    @DisplayName("Variant documentation tests")
    Stream<DynamicContainer> testVariants() throws IOException {
        List<Path> variants = findVariants();
        assumeFalse(variants.isEmpty(), "No variants found");

        return variants.stream().map(variantFile -> {
            String variantName = variantFile.getParent().getFileName().toString();
            return dynamicContainer(variantName, Stream.of(
                dynamicTest("should load without errors",        () -> checkLoads(variantFile)),
                dynamicTest("should have proper title",          this::checkTitle),
                dynamicTest("should have table of contents",     this::checkTableOfContents),
                dynamicTest("should have main content sections", this::checkMainSections),
                dynamicTest("internal links should work",        this::checkInternalLinks),
                dynamicTest("should not have broken images",     this::checkImages),
                dynamicTest("should have code blocks",           this::checkCodeBlocks)
            ));
        });
    }

    private void checkLoads(Path variantFile) {
        navigateTo(variantFile);

        String title = page.title();
        assertNotNull(title, "Variant should have a title");
        assertFalse(title.isEmpty(), "Variant title should not be empty");
    }

    private void checkTitle() {
        String title = page.title();
        assertTrue(title.toLowerCase().contains("quarkus") || title.toLowerCase().contains("workshop"),
            "Title should contain 'Quarkus' or 'Workshop', but was: " + title);
    }

    private void checkTableOfContents() {
        Locator toc = page.locator("#toc, .toc, nav");
        assertTrue(toc.count() > 0, "Should have a table of contents");
    }

    private void checkMainSections() {
        int h2Count = page.locator("h2").count();
        assertTrue(h2Count >= 5,
            "Should have at least 5 top-level sections, but found " + h2Count);

        int sectionCount = page.locator(".sect1").count();
        assertTrue(sectionCount >= 5,
            "Should have at least 5 sect1 blocks, but found " + sectionCount);
    }

    private void checkInternalLinks() {
        Set<String> brokenLinks = findBrokenInternalLinks();
        assertTrue(brokenLinks.isEmpty(),
            "Has broken internal links: " + String.join(", ", brokenLinks));
    }

    private void checkImages() {
        Set<String> brokenImages = findBrokenImages();
        assertTrue(brokenImages.isEmpty(),
            "Has broken images: " + String.join(", ", brokenImages));
    }

    private void checkCodeBlocks() {
        Locator codeBlocks = page.locator("pre code, .listingblock, .code");
        assertTrue(codeBlocks.count() > 0, "Should have code blocks");
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
