package io.quarkus.workshop.docs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.BeforeParameterizedClassInvocation;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
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
@Execution(ExecutionMode.CONCURRENT)
public class VariantsTest extends DocumentationTestBase {

    static final File VARIANTS_PATH = new File(DOCS_BASE_PATH, "variants");

    static class PageHolder implements AutoCloseable {
        BrowserContext context;
        Page page;

        @Override
        public void close() {
            if (context != null) {
                context.close();
            }
        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Parameter(0)
    Path variantFile;

    @Parameter(1)
    PageHolder holder;

    static List<Arguments> findVariants() throws IOException {
        Path variantsDir = VARIANTS_PATH.toPath();

        if (!Files.exists(variantsDir)) {
            return List.of();
        }

        List<Arguments> variants = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(variantsDir, 2)) {
            paths.filter(Files::isDirectory)
                .filter(p -> !p.equals(variantsDir))
                .forEach(dir -> {
                    Path spineFile = dir.resolve(SPINE_HTML);
                    if (Files.exists(spineFile)) {
                        variants.add(Arguments.of(
                            Named.of(dir.getFileName().toString(), spineFile),
                            new PageHolder()));
                    }
                });
        }

        return variants;
    }

    @BeforeParameterizedClassInvocation
    static void setupVariant(Path variantFile, PageHolder holder) {
        Browser b = getOrCreateBrowser();
        holder.context = b.newContext();
        holder.page = holder.context.newPage();
        holder.page.navigate("file://" + variantFile.toAbsolutePath());
        holder.page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    @Override
    @BeforeEach
    void createContextAndPage() {
        context = holder.context;
        page = holder.page;
    }

    @Override
    @AfterEach
    void closeContext() {
        // managed by PageHolder.close() via autoCloseArguments
    }

    @AfterAll
    static void closeBrowser() {
        // Each worker thread creates its own Browser via ThreadLocal, but @AfterAll only runs
        // on one thread, so we can't close browsers from other threads here. Fixing this would
        // require tracking all browsers in a concurrent collection. The leak is bounded by the
        // thread pool size and cleaned up on JVM exit, so we accept it.
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void shouldLoadWithoutErrors() {
        String title = page.title();
        assertNotNull(title, "Variant should have a title");
        assertFalse(title.isEmpty(), "Variant title should not be empty");
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void shouldHaveProperTitle() {
        String title = page.title();
        assertTrue(title.toLowerCase().contains("quarkus") || title.toLowerCase().contains("workshop"),
            "Title should contain 'Quarkus' or 'Workshop', but was: " + title);
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void shouldHaveTableOfContents() {
        Locator toc = page.locator("#toc, .toc, nav");
        assertTrue(toc.count() > 0, "Should have a table of contents");
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void shouldHaveMainSections() {
        int h2Count = page.locator("h2").count();
        assertTrue(h2Count >= 5,
            "Should have at least 5 top-level sections, but found " + h2Count);

        int sectionCount = page.locator(".sect1").count();
        assertTrue(sectionCount >= 5,
            "Should have at least 5 sect1 blocks, but found " + sectionCount);
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void internalLinksShouldWork() {
        Set<String> brokenLinks = findBrokenInternalLinks();
        assertTrue(brokenLinks.isEmpty(),
            "Has broken internal links: " + String.join(", ", brokenLinks));
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void shouldNotHaveBrokenImages() {
        Set<String> brokenImages = findBrokenImages();
        assertTrue(brokenImages.isEmpty(),
            "Has broken images: " + String.join(", ", brokenImages));
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void shouldHaveCodeBlocks() {
        Locator codeBlocks = page.locator("pre code, .listingblock, .code");
        assertTrue(codeBlocks.count() > 0, "Should have code blocks");
    }
}
