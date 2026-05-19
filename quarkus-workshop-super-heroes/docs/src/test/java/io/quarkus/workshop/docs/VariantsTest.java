package io.quarkus.workshop.docs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.AfterParameterizedClassInvocation;
import org.junit.jupiter.params.BeforeParameterizedClassInvocation;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for documentation variants.
 * These tests can only be run after variants are generated, which is time-consuming.
 * Tests are skipped when the variants directory does not exist.
 */
@ParameterizedClass(name = "{0}", allowZeroInvocations = true)
@MethodSource("findVariants")
public class VariantsTest extends DocumentationTestBase {

    static final File VARIANTS_PATH = new File(DOCS_BASE_PATH, "variants");

    private static BrowserContext sharedContext;
    private static Page sharedPage;

    @Parameter
    Path variantFile;

    static List<Named<Path>> findVariants() throws IOException {
        Path variantsDir = VARIANTS_PATH.toPath();

        if (!Files.exists(variantsDir)) {
            return List.of();
        }

        List<Named<Path>> variants = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(variantsDir, 2)) {
            paths.filter(Files::isDirectory)
                .filter(p -> !p.equals(variantsDir))
                .forEach(dir -> {
                    Path spineFile = dir.resolve(SPINE_HTML);
                    if (Files.exists(spineFile)) {
                        variants.add(Named.of(dir.getFileName().toString(), spineFile));
                    }
                });
        }

        return variants;
    }

    @BeforeParameterizedClassInvocation
    static void setupVariant(Path variantFile) {
        sharedContext = browser.newContext();
        sharedPage = sharedContext.newPage();
        sharedPage.navigate("file://" + variantFile.toAbsolutePath());
        sharedPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    @AfterParameterizedClassInvocation
    static void teardownVariant() {
        if (sharedContext!=null) {
            sharedContext.close();
            sharedContext = null;
            sharedPage = null;
        }
    }

    @Override
    @BeforeEach
    void createContextAndPage() {
        context = sharedContext;
        page = sharedPage;
    }

    @Override
    @AfterEach
    void closeContext() {
        // managed per invocation, not per test
    }

    @Test
    void shouldLoadWithoutErrors() {
        String title = page.title();
        assertNotNull(title, "Variant should have a title");
        assertFalse(title.isEmpty(), "Variant title should not be empty");
    }

    @Test
    void shouldHaveProperTitle() {
        String title = page.title();
        assertTrue(title.toLowerCase().contains("quarkus") || title.toLowerCase().contains("workshop"),
            "Title should contain 'Quarkus' or 'Workshop', but was: " + title);
    }

    @Test
    void shouldHaveTableOfContents() {
        Locator toc = page.locator("#toc, .toc, nav");
        assertTrue(toc.count() > 0, "Should have a table of contents");
    }

    @Test
    void shouldHaveMainSections() {
        int h2Count = page.locator("h2").count();
        assertTrue(h2Count >= 5,
            "Should have at least 5 top-level sections, but found " + h2Count);

        int sectionCount = page.locator(".sect1").count();
        assertTrue(sectionCount >= 5,
            "Should have at least 5 sect1 blocks, but found " + sectionCount);
    }

    @Test
    void internalLinksShouldWork() {
        Set<String> brokenLinks = findBrokenInternalLinks();
        assertTrue(brokenLinks.isEmpty(),
            "Has broken internal links: " + String.join(", ", brokenLinks));
    }

    @Test
    void shouldNotHaveBrokenImages() {
        Set<String> brokenImages = findBrokenImages();
        assertTrue(brokenImages.isEmpty(),
            "Has broken images: " + String.join(", ", brokenImages));
    }

    @Test
    void shouldHaveCodeBlocks() {
        Locator codeBlocks = page.locator("pre code, .listingblock, .code");
        assertTrue(codeBlocks.count() > 0, "Should have code blocks");
    }
}
