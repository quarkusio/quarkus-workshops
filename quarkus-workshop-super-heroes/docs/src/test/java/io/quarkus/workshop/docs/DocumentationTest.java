package io.quarkus.workshop.docs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import io.quarkiverse.roq.testing.RoqAndRoll;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Assertions about the spine, the single page that stitches the whole workshop together.
 */
@QuarkusTest
@RoqAndRoll(port = RoqSiteTest.ROQ_PORT)
public class DocumentationTest extends RoqSiteTest {

    @Test
    @DisplayName("Main spine/index.html should load without errors")
    void testSpineLoadsSuccessfully() {
        Response response = page.navigate(url(SPINE_HTML));
        assertNotNull(response, "Page should load");
        assertTrue(response.ok(),
            "Page should load successfully (status: " + response.status() + ")");
    }

    @Test
    @DisplayName("Main spine/index.html should have proper title")
    void testSpineHasTitle() {
        navigateToSpine();

        String title = page.title();
        assertNotNull(title, "Page should have a title");
        assertFalse(title.isEmpty(), "Title should not be empty");
        assertTrue(title.toLowerCase().contains("quarkus") || title.toLowerCase().contains("workshop"),
            "Title should contain 'Quarkus' or 'Workshop', but was: " + title);
    }

    @Test
    @DisplayName("Main spine/index.html should not have unescaped version attribute in the title")
    void testSpineHasResolvedVersion() {
        navigateToSpine();

        Locator titles = page.locator("h1");
        String title = titles.innerText();
        assertNotNull(title, "Page should have a title");
        assertFalse(title.isEmpty(), "Title should not be empty");
        assertFalse(title.toLowerCase().contains("{quarkus"),
            "Title should not contain unresolved attribute, but was: " + title);
    }

    @Test
    @DisplayName("Main spine/index.html should not have unescaped version attribute in the body")
    void testSpineBodyHasResolvedVersion() {
        navigateToSpine();

        Locator allp = page.locator("p");
        List<Locator> ps = allp.all();
        for (Locator p : ps) {
            String text = p.innerText();
            assertFalse(text.toLowerCase().contains("-version}"),
                "Body should not contain unresolved version attributes but found : " + text);
        }
    }

    @Test
    @DisplayName("Main spine/index.html should have table of contents")
    void testSpineHasTableOfContents() {
        navigateToSpine();

        Locator toc = page.locator("#toc, .toc, nav");
        assertTrue(toc.count() > 0, "Page should have a table of contents");
    }

    @Test
    @DisplayName("Main spine/index.html should have main content sections")
    void testSpineHasMainSections() {
        navigateToSpine();

        int h2Count = page.locator("h2").count();
        assertTrue(h2Count >= 10,
            "Full spine should have at least 10 top-level sections, but found " + h2Count);

        int sectionCount = page.locator(".sect1").count();
        assertTrue(sectionCount >= 10,
            "Full spine should have at least 10 sect1 blocks, but found " + sectionCount);
    }

    @Test
    @DisplayName("Main spine/index.html should have code blocks")
    void testSpineHasCodeBlocks() {
        navigateToSpine();

        Locator codeBlocks = page.locator("pre code, .listingblock, .code");
        assertTrue(codeBlocks.count() > 0, "Page should have code blocks");
    }

    @Test
    @DisplayName("Main spine/index.html internal links should work")
    void testSpineInternalLinksWork() {
        navigateToSpine();

        Set<String> brokenLinks = findBrokenInternalLinks();

        assertTrue(brokenLinks.isEmpty(),
            "Found broken internal links: " + String.join(", ", brokenLinks));
    }

    @Test
    @DisplayName("Main spine/index.html should not have broken image references")
    void testSpineImagesLoad() {
        navigateToSpine();

        Set<String> deadImages = findDeadImages();

        assertTrue(deadImages.isEmpty(),
            "Found dead images: " + String.join(", ", deadImages));
    }

    @Test
    @DisplayName("Main spine/index.html external links should be valid URLs")
    void testSpineExternalLinksAreValid() {
        navigateToSpine();

        Locator externalLinks = page.locator("a[href^='http://'], a[href^='https://']");
        int linkCount = externalLinks.count();

        Set<String> invalidUrls = new HashSet<>();

        for (int i = 0; i < linkCount; i++) {
            String href = externalLinks.nth(i).getAttribute("href");
            if (href!=null && !isValidUrl(href)) {
                invalidUrls.add(href);
            }
        }

        assertTrue(invalidUrls.isEmpty(),
            "Found invalid external URLs: " + String.join(", ", invalidUrls));
    }

    @Test
    @DisplayName("Main spine/index.html should have proper heading hierarchy")
    void testSpineHeadingHierarchy() {
        navigateToSpine();

        Locator h1 = page.locator("h1");
        Locator h2 = page.locator("h2");
        Locator h3 = page.locator("h3");

        int h1Count = h1.count();
        int h2Count = h2.count();
        int h3Count = h3.count();

        assertTrue(h1Count > 0, "Document should have at least one h1");

        if (h3Count > 0) {
            assertTrue(h2Count > 0, "If h3 exists, h2 should also exist");
        }
    }

    @Test
    @DisplayName("Main spine/index.html should contain key workshop sections")
    void testSpineHasKeyContent() {
        navigateToSpine();

        String pageContent = page.content();

        assertTrue(pageContent.contains("Villain Microservice") ||
                pageContent.contains("Villain microservice") ||
                pageContent.contains("villain microservice"),
            "Page should contain 'Villain Microservice' section");

        assertTrue(pageContent.contains("Hero Microservice") ||
                pageContent.contains("Hero microservice") ||
                pageContent.contains("hero microservice"),
            "Page should contain 'Hero Microservice' section");

        assertTrue(pageContent.contains("Fight Microservice") ||
                pageContent.contains("Fight microservice") ||
                pageContent.contains("fight microservice"),
            "Page should contain 'Fight Microservice' section");
    }

    @Test
    @DisplayName("Main spine/index.html should contain warming cache appendix content")
    void testSpineHasWarmingCacheAppendix() {
        navigateToSpine();

        String pageContent = page.content();

        assertTrue(pageContent.contains("Warming the caches") ||
                pageContent.contains("warming the caches"),
            "Page should contain 'Warming the caches' appendix section");

        // Verify the nested include content is present (this would fail if nested includes broke)
        assertTrue(pageContent.contains("Warming up Maven") ||
                pageContent.contains("warming up Maven") ||
                pageContent.contains("./mvnw clean install"),
            "Page should contain Maven warming content from appendix-preparing-warming-maven.adoc");

        assertTrue(pageContent.contains("Warming up Docker") ||
                pageContent.contains("warming up Docker") ||
                pageContent.contains("docker compose"),
            "Page should contain Docker warming content from appendix-preparing-warming-docker.adoc");
    }

    @Test
    @DisplayName("Main spine/index.html should not have asciidoc include errors for warming appendix files")
    void testSpineHasNoIncludeErrorsForWarmingAppendix() {
        navigateToSpine();

        String pageContent = page.content();

        // Asciidoctor logs SEVERE errors for failed includes but may not always render them in HTML.
        // This test verifies the warming appendix files and their nested includes are resolved correctly.
        // These files use relative paths (../common-no-zip-*.adoc) which require proper base-dir support.

        // Check for common error messages that might appear in the rendered output
        assertFalse(pageContent.contains("Unresolved directive") &&
                (pageContent.contains("common-no-zip-download") ||
                 pageContent.contains("common-no-zip-infrastructure")),
            "Page should not contain unresolved include directives for common-no-zip files");

        assertFalse(pageContent.contains("Include file not found") &&
                (pageContent.contains("common-no-zip-download") ||
                 pageContent.contains("common-no-zip-infrastructure") ||
                 pageContent.contains("warming-maven") ||
                 pageContent.contains("warming-docker")),
            "Page should not contain 'Include file not found' errors for warming appendix files");

        // Verify the file paths don't appear as literal text (which would indicate broken includes)
        assertFalse(pageContent.contains("appendix-preparing-warming-maven.adoc"),
            "Page should not contain literal .adoc filename (indicates broken include)");
        assertFalse(pageContent.contains("appendix-preparing-warming-docker.adoc"),
            "Page should not contain literal .adoc filename (indicates broken include)");
    }

    private void navigateToSpine() {
        navigateTo(url(SPINE_HTML));
    }
}
