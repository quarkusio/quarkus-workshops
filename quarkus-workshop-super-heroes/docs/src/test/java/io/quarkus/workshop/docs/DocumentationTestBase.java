package io.quarkus.workshop.docs;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class DocumentationTestBase {

    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;

    protected static final String DOCS_BASE_PATH = System.getProperty("docs.base.path", "target/generated-asciidoc/");
    protected static final String SPINE_HTML = "spine.html";

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)([\\w.-]+)(:[0-9]+)?(/.*)?$",
        Pattern.CASE_INSENSITIVE
    );

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    protected void navigateTo(Path htmlFile) {
        String fileUrl = "file://" + htmlFile.toAbsolutePath();
        page.navigate(fileUrl);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    protected Set<String> findBrokenInternalLinks() {
        Locator internalLinks = page.locator("a[href^='#']");
        int linkCount = internalLinks.count();
        Set<String> brokenLinks = new HashSet<>();

        for (int i = 0; i < linkCount; i++) {
            String href = internalLinks.nth(i).getAttribute("href");
            if (href != null && !href.isEmpty()) {
                String targetId = href.substring(1);
                // Use attribute selector — bare #id breaks on AsciiDoc IDs containing dots or colons
                Locator target = page.locator("[id='" + escapeAttrValue(targetId) + "']");
                if (target.count() == 0) {
                    brokenLinks.add(href);
                }
            }
        }

        return brokenLinks;
    }

    protected Set<String> findBrokenImages() {
        Locator images = page.locator("img");
        int imageCount = images.count();
        Set<String> brokenImages = new HashSet<>();

        for (int i = 0; i < imageCount; i++) {
            Locator img = images.nth(i);
            String src = img.getAttribute("src");

            if (src != null && !src.isEmpty()) {
                try {
                    BoundingBox box = img.boundingBox();
                    if (box == null || (box.width == 0 && box.height == 0)) {
                        Object naturalWidth = img.evaluate("img => img.naturalWidth");
                        if (naturalWidth == null || ((Number) naturalWidth).intValue() == 0) {
                            brokenImages.add(src);
                        }
                    }
                } catch (Exception e) {
                    brokenImages.add(src + " (error: " + e.getMessage() + ")");
                }
            }
        }

        return brokenImages;
    }

    protected boolean isValidUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }

    private String escapeAttrValue(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
