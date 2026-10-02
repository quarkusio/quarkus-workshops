package io.quarkus.workshop.docs;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * A browser, a page, and assertions about what is on it.
 * <p>
 * Pages are always loaded over http rather than {@code file://}, so that document-root-relative
 * urls ({@code src="/images/..."}) resolve the way they do on the published site, and so a missing
 * resource is distinguishable from one the browser simply declined to fetch. Subclasses say where
 * that http server is: {@link RoqSiteTest} generates the site in-process during the test phase,
 * {@link BrowserIT} serves the fully assembled output during the integration-test phase.
 */
public abstract class BrowserTestBase {

    protected static final String SPINE_HTML = "spine/index.html";

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)([\\w.-]+)(:[0-9]+)?(/.*)?$",
        Pattern.CASE_INSENSITIVE
    );

    // Playwright objects are not thread safe and the ITs run with classes in parallel, so each
    // worker thread gets its own. The browsers other threads create cannot be closed from
    // @AfterAll, which only runs on one of them; the leak is bounded by the thread pool size and
    // cleaned up on JVM exit.
    private static final ThreadLocal<Playwright> playwright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browser = new ThreadLocal<>();

    protected BrowserContext context;
    protected Page page;

    /** Where the site under test is served from, with no trailing slash. */
    protected abstract String baseUrl();

    static Browser getOrCreateBrowser() {
        if (browser.get()==null) {
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
        if (b!=null) {
            b.close();
            browser.remove();
        }
        Playwright p = playwright.get();
        if (p!=null) {
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
        if (context!=null) {
            context.close();
        }
    }

    /** The url a path inside the site is served at, e.g. {@code spine/index.html}. */
    protected String url(String sitePath) {
        return baseUrl() + "/" + (sitePath.startsWith("/") ? sitePath.substring(1):sitePath);
    }

    protected void navigateTo(String url) {
        navigateTo(page, url);
    }

    static void navigateTo(Page page, String url) {
        page.navigate(url);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    /**
     * Every {@code <img>} on the current page whose source does not actually serve an image,
     * reported as {@code src -> HTTP status}.
     * <p>
     * Only same-origin images are checked. External ones would make the build depend on the
     * network being up and on other people's sites staying put, which is a different test.
     */
    protected Set<String> findDeadImages() {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> images = (List<Map<String, Object>>) page.evaluate("""
            () => Array.from(document.images).map(img => ({
                src: img.getAttribute('src'),
                resolved: img.src
            }))
            """);

        // A sorted set so a page with the same missing image twice reports it once, and so the
        // failure message is stable enough to diff between runs.
        Set<String> dead = new TreeSet<>();
        String origin = baseUrl() + "/";

        for (Map<String, Object> image : images) {
            String src = (String) image.get("src");
            String resolved = (String) image.get("resolved");
            if (resolved==null || !resolved.startsWith(origin)) {
                continue;
            }
            // Fetch rather than inspect naturalWidth: the answer does not then depend on whether
            // the browser has got round to loading the image yet.
            APIResponse response = page.request().get(resolved);
            try {
                if (!response.ok()) {
                    dead.add(src + " -> HTTP " + response.status());
                } else if (response.body().length==0) {
                    dead.add(src + " -> empty");
                }
            } finally {
                response.dispose();
            }
        }

        return dead;
    }

    protected Set<String> findBrokenInternalLinks() {
        Locator internalLinks = page.locator("a[href^='#']");
        int linkCount = internalLinks.count();
        Set<String> brokenLinks = new HashSet<>();

        for (int i = 0; i < linkCount; i++) {
            String href = internalLinks.nth(i).getAttribute("href");
            if (href!=null && !href.isEmpty()) {
                String targetId = href.substring(1);
                // Use attribute selector — bare #id breaks on AsciiDoc IDs containing dots or colons
                Locator target = page.locator("[id='" + escapeAttrValue(targetId) + "']");
                if (target.count()==0) {
                    brokenLinks.add(href);
                }
            }
        }

        return brokenLinks;
    }

    protected boolean isValidUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }

    private String escapeAttrValue(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
