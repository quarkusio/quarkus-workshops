package io.quarkus.workshop.docs;

import java.io.File;
import java.nio.file.Path;

/**
 * Base for the tests that run in the {@code integration-test} phase against the fully assembled
 * site in {@code target/roq} — the canonical pages plus every generated variant.
 * <p>
 * {@link SiteServer} serves {@link #DOCS_BASE_PATH} by default; {@code -Dtest.url} points the
 * tests at an already-running server instead. Tests that only need the canonical site should
 * extend {@link RoqSiteTest} and run in the much faster {@code test} phase.
 */
public abstract class BrowserIT extends BrowserTestBase {

    /** Root of the generated site under test. */
    protected static final File DOCS_BASE_PATH = new File(System.getProperty("docs.base.path", "target/roq/"));

    /**
     * The url prefix the site under test is published under, with a leading and no trailing
     * slash, or empty for a site at the document root. This is the {@code docs.site-path} the site
     * was generated with: its asset urls are absolute and carry the prefix, so the tests have to
     * ask for its pages under the prefix too.
     */
    protected static final String SITE_PATH = resolveSitePath();

    /** Where that site is served from, with no trailing slash. Includes {@link #SITE_PATH}. */
    protected static final String BASE_URL = resolveBaseUrl();

    private static String resolveSitePath() {
        String path = System.getProperty("docs.site-path", "").trim();
        if (path.isEmpty() || "/".equals(path)) {
            return "";
        }
        String withLeadingSlash = path.startsWith("/") ? path : "/" + path;
        return withLeadingSlash.endsWith("/")
            ? withLeadingSlash.substring(0, withLeadingSlash.length() - 1)
            : withLeadingSlash;
    }

    // Resolved in a static initialiser rather than @BeforeAll because @ParameterizedClass
    // invocation setup and @MethodSource factories can both run before any @BeforeAll of ours.
    private static String resolveBaseUrl() {
        String url = System.getProperty("test.url");
        if (url == null || url.isBlank()) {
            // SiteServer mounts the site under the prefix and returns a url that includes it.
            return SiteServer.start(DOCS_BASE_PATH, SITE_PATH);
        }
        String origin = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        // -Dtest.url names the server, not the site within it, so the prefix still applies.
        return origin.endsWith(SITE_PATH) ? origin : origin + SITE_PATH;
    }

    @Override
    protected String baseUrl() {
        return BASE_URL;
    }

    /** The url a path inside the site is served at, for callers with no instance to hand. */
    protected static String siteUrl(String sitePath) {
        return BASE_URL + "/" + (sitePath.startsWith("/") ? sitePath.substring(1):sitePath);
    }

    /** The url a generated file is served at. The file has to live under {@link #DOCS_BASE_PATH}. */
    protected static String urlFor(Path generatedFile) {
        Path root = DOCS_BASE_PATH.toPath().toAbsolutePath().normalize();
        Path relative = root.relativize(generatedFile.toAbsolutePath().normalize());
        return siteUrl(relative.toString().replace(File.separatorChar, '/'));
    }

    protected void navigateTo(Path generatedFile) {
        navigateTo(page, urlFor(generatedFile));
    }
}
