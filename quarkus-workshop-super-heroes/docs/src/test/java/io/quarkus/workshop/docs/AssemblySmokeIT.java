package io.quarkus.workshop.docs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The one test that needs the whole build: pick an OS on the configurator, press the button, and
 * end up on a workshop.
 * <p>
 * Everything else about the configurator is checked in {@link ConfiguratorTest} against a site
 * Roq generates in-process, which is fast but has no {@code variants/} in it. What can only break
 * once the pieces are assembled is the join between the two halves: the configurator builds a url
 * out of the selected options, and a separate Maven iteration builds a directory per variant, and
 * nothing but this test says the two agree on the name.
 * <p>
 * Which OSes are checked follows what the build asked for. {@code -Dos} picks one, and the docs
 * job in build.yml runs a matrix shard per OS, so each shard checks its own. The assembled site
 * has every shard's variants merged into it, and the job that tests it passes
 * {@code -Dvariants.complete=true} to say so: that checks every OS, and will not let a missing
 * variant pass as a skip.
 * <p>
 * Agreeing on the name is checked in every build, because even a quick build lays down a
 * directory per variant. Actually following the link needs the variant sites rendered, which
 * happens under {@code -Dfull}; without it that half is skipped rather than failed.
 */
public class AssemblySmokeIT extends BrowserIT {

    private static final Path BUILT_VARIANTS = new File(DOCS_BASE_PATH, "variants").toPath();

    /** The OS this build generated variants for; see the {@code --os} filter in generate-variants.java. */
    private static final String BUILT_OS = System.getProperty("os", "all");

    /** Set when the site under test has every OS's variants in it, so nothing may be skipped. */
    private static final boolean VARIANTS_COMPLETE = Boolean.getBoolean("variants.complete");

    static List<String> osOptions() {
        return VARIANTS_COMPLETE ? VariantsConfig.load().osOptions():List.of(BUILT_OS);
    }

    @ParameterizedTest(name = "os={0}")
    @MethodSource("osOptions")
    @DisplayName("Configurator should send each OS to a variant the build produced")
    void customWorkshopResolvesForOs(String os) {
        navigateTo(siteUrl("index.html"));

        page.locator("input[id='mavenRadio']").check();
        page.locator("input[id='" + os + "Radio']").check();

        String variantUrl = followCustomWorkshopButton();
        String variantName = variantName(variantUrl);

        assertTrue(variantName.contains("os-" + os),
            "Configurator should encode the selected OS in the url, but got: " + variantUrl);
        assertTrue(Files.isDirectory(BUILT_VARIANTS.resolve(variantName)),
            "Configurator sent us to a variant the build does not produce: " + variantName
                + "\nurl: " + variantUrl
                + "\nThe configurator and generate-variants.java have drifted apart on how a"
                + " variant is named.");

        if (!VARIANTS_COMPLETE) {
            // A quick build lays down the variant directories but renders no spine into them.
            Path spine = BUILT_VARIANTS.resolve(variantName).resolve(SPINE_HTML);
            Assumptions.assumeTrue(Files.isRegularFile(spine),
                "Variant sites are only rendered with -Dfull; nothing at " + spine);
        }

        assertVariantPageIsAWorkshop(variantUrl);
    }

    /**
     * Clicks through to the custom workshop and returns where we landed. The button navigates by
     * assigning {@code window.location}, so there is no request to wait on until the url has
     * actually changed.
     */
    private String followCustomWorkshopButton() {
        String before = page.url();

        Locator customButton = page.locator("button:has-text('Take me to my custom workshop')");
        assertTrue(customButton.count() > 0, "Configurator should have a custom workshop button");
        customButton.click();

        page.waitForURL(url -> !url.equals(before), new Page.WaitForURLOptions().setTimeout(20_000));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        String after = page.url();
        assertFalse(after.startsWith("chrome-error:"), "Navigation failed: " + after);
        return after;
    }

    private void assertVariantPageIsAWorkshop(String variantUrl) {
        APIResponse response = page.request().get(variantUrl);
        try {
            assertTrue(response.ok(),
                "Variant page should be served, but got HTTP " + response.status() + " for " + variantUrl);
        } finally {
            response.dispose();
        }

        String title = page.title();
        assertNotNull(title, "Variant should have a title");
        assertTrue(title.toLowerCase().contains("quarkus") || title.toLowerCase().contains("workshop"),
            "Variant title should mention Quarkus or the workshop, but was: " + title);

        int sections = page.locator(".sect1").count();
        assertTrue(sections >= 5,
            "Variant should be a whole workshop, but only found " + sections + " sections at " + variantUrl);
    }

    /** The variant name out of a {@code .../variants/<name>/spine/} url. */
    private static String variantName(String variantUrl) {
        String marker = "/variants/";
        int start = variantUrl.indexOf(marker);
        assertTrue(start >= 0, "Expected a /variants/ url, but got: " + variantUrl);
        start += marker.length();
        int end = variantUrl.indexOf('/', start);
        return end < 0 ? variantUrl.substring(start):variantUrl.substring(start, end);
    }

}
