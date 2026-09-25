package io.quarkus.workshop.docs;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class DocumentationTestBase {

    private static final ThreadLocal<Playwright> playwright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browser = new ThreadLocal<>();
    protected BrowserContext context;
    protected Page page;

    protected static final File DOCS_BASE_PATH = new File(System.getProperty("docs.base.path", "target/generated-asciidoc/"));
    protected static final String SPINE_HTML = "spine.html";

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)([\\w.-]+)(:[0-9]+)?(/.*)?$",
        Pattern.CASE_INSENSITIVE
    );

    static Browser getOrCreateBrowser() {
        if (browser.get() == null) {
            Playwright pw = Playwright.create();
            playwright.set(pw);
            browser.set(pw.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true)));
        }
        return browser.get();
    }

    @BeforeAll
    static void launchBrowser() {
        getOrCreateBrowser();
    }

    @AfterAll
    static void closeBrowser() {
        Browser b = browser.get();
        if (b != null) {
            b.close();
            browser.remove();
        }
        Playwright p = playwright.get();
        if (p != null) {
            p.close();
            playwright.remove();
        }
    }

    @BeforeEach
    void createContextAndPage() {
        context = getOrCreateBrowser().newContext();
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
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
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
