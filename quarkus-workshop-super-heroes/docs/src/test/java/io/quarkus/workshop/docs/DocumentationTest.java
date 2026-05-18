package io.quarkus.workshop.docs;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentationTest extends DocumentationTestBase {

    @Test
    @DisplayName("Main spine.html file should exist")
    void testSpineFileExists() {
        Path spineFile = new File(DOCS_BASE_PATH, SPINE_HTML).toPath();
        assertTrue(Files.exists(spineFile), "spine.html should exist at " + spineFile);
    }

    @Test
    @DisplayName("Main spine.html should load without errors")
    void testSpineLoadsSuccessfully() {
        String fileUrl = "file://" + new File(DOCS_BASE_PATH, SPINE_HTML).toPath().toAbsolutePath();

        Response response = page.navigate(fileUrl);
        assertNotNull(response, "Page should load");
        assertTrue(response.ok() || response.status() == 0,
            "Page should load successfully (status: " + response.status() + ")");
    }

    @Test
    @DisplayName("Main spine.html should have proper title")
    void testSpineHasTitle() {
        navigateToSpine();

        String title = page.title();
        assertNotNull(title, "Page should have a title");
        assertFalse(title.isEmpty(), "Title should not be empty");
        assertTrue(title.toLowerCase().contains("quarkus") || title.toLowerCase().contains("workshop"),
            "Title should contain 'Quarkus' or 'Workshop', but was: " + title);
    }

    @Test
    @DisplayName("Main spine.html should have table of contents")
    void testSpineHasTableOfContents() {
        navigateToSpine();

        Locator toc = page.locator("#toc, .toc, nav");
        assertTrue(toc.count() > 0, "Page should have a table of contents");
    }

    @Test
    @DisplayName("Main spine.html should have main content sections")
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
    @DisplayName("Main spine.html should have code blocks")
    void testSpineHasCodeBlocks() {
        navigateToSpine();

        Locator codeBlocks = page.locator("pre code, .listingblock, .code");
        assertTrue(codeBlocks.count() > 0, "Page should have code blocks");
    }

    @Test
    @DisplayName("Main spine.html internal links should work")
    void testSpineInternalLinksWork() {
        navigateToSpine();

        Set<String> brokenLinks = findBrokenInternalLinks();

        assertTrue(brokenLinks.isEmpty(),
            "Found broken internal links: " + String.join(", ", brokenLinks));
    }

    @Test
    @DisplayName("Main spine.html should not have broken image references")
    void testSpineImagesLoad() {
        navigateToSpine();

        Set<String> brokenImages = findBrokenImages();

        assertTrue(brokenImages.isEmpty(),
            "Found broken images: " + String.join(", ", brokenImages));
    }

    @Test
    @DisplayName("Main spine.html external links should be valid URLs")
    void testSpineExternalLinksAreValid() {
        navigateToSpine();

        Locator externalLinks = page.locator("a[href^='http://'], a[href^='https://']");
        int linkCount = externalLinks.count();

        Set<String> invalidUrls = new HashSet<>();

        for (int i = 0; i < linkCount; i++) {
            String href = externalLinks.nth(i).getAttribute("href");
            if (href != null && !isValidUrl(href)) {
                invalidUrls.add(href);
            }
        }

        assertTrue(invalidUrls.isEmpty(),
            "Found invalid external URLs: " + String.join(", ", invalidUrls));
    }

    @Test
    @DisplayName("Main spine.html should have proper heading hierarchy")
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
    @DisplayName("Main spine.html should contain key workshop sections")
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

    private void navigateToSpine() {
        navigateTo(new File(DOCS_BASE_PATH, SPINE_HTML).toPath());
    }
}
