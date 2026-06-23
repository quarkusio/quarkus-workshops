package io.quarkus.workshop.docs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that validate all combinations on the configurator index.html page
 * resolve to valid documentation pages.
 * <p>
 * Flag definitions, constraints, and standalone markers are read from
 * variants-config.json (the single source of truth).
 */
public class ConfiguratorTest extends DocumentationTestBase {

    private static final String INDEX_HTML = "index.html";

    private static final String[] ALL_OS_OPTIONS;
    private static final String[] OS_OPTIONS;
    private static final String[] ALL_BT_OPTIONS;
    private static final String[] BT_OPTIONS;

    static {
        VariantsConfig config = VariantsConfig.load();
        ALL_OS_OPTIONS = config.osOptions().toArray(new String[0]);
        OS_OPTIONS = getOsOptionsToTest(ALL_OS_OPTIONS);
        ALL_BT_OPTIONS = config.buildToolOptions().toArray(new String[0]);
        BT_OPTIONS = getBuildToolOptionsToTest(ALL_BT_OPTIONS);
    }

    @Test
    @DisplayName("Configurator index.html should exist")
    void testIndexFileExists() {
        Path indexFile = new File(DOCS_BASE_PATH, INDEX_HTML).toPath();
        assertTrue(Files.exists(indexFile), "index.html should exist at " + indexFile);
    }

    @Test
    @DisplayName("Configurator index.html should load without errors")
    void testIndexLoadsSuccessfully() {
        String fileUrl = "file://" + new File(DOCS_BASE_PATH, INDEX_HTML).toPath().toAbsolutePath();

        Response response = page.navigate(fileUrl);
        assertNotNull(response, "Page should load");
        assertTrue(response.ok() || response.status()==0,
            "Page should load successfully (status: " + response.status() + ")");
    }

    @Test
    @DisplayName("Configurator should have default workshop button")
    void testIndexHasDefaultButton() {
        navigateToIndex();

        Locator defaultButton = page.locator("button:has-text('Take me to the default workshop')");
        assertTrue(defaultButton.count() > 0, "Should have default workshop button");
    }

    @Test
    @DisplayName("Configurator should have custom workshop button")
    void testIndexHasCustomButton() {
        navigateToIndex();

        Locator customButton = page.locator("button:has-text('Take me to my custom workshop')");
        assertTrue(customButton.count() > 0, "Should have custom workshop button");
    }

    @Test
    @DisplayName("Configurator should have select all / none links")
    void testIndexHasSelectAllAndNoneLinks() {
        navigateToIndex();

        Locator selectAllLink = page.locator("a:has-text('Select all')");
        assertTrue(selectAllLink.count() > 0, "Should have select all link");

        Locator noneLink = page.locator("a:has-text('none')");
        assertTrue(noneLink.count() > 0, "Should have none link");
    }

    @Test
    @DisplayName("Configurator should have OS selection options")
    void testIndexHasOsOptions() {
        navigateToIndex();

        for (String os : new String[]{"mac", "windows", "linux"}) {
            Locator osRadio = page.locator("input[id='" + os + "Radio']");
            assertEquals(1, osRadio.count(), "Should have " + os + " radio button");
        }
    }

    @Test
    @DisplayName("Configurator should have build tool selection options")
    void testIndexHasBuildToolOptions() {
        navigateToIndex();

        Locator mavenRadio = page.locator("input[id='mavenRadio'][name='buildToolOption']");
        assertEquals(1, mavenRadio.count(), "Should have Maven build tool radio button");

        Locator gradleRadio = page.locator("input[id='gradleRadio'][name='buildToolOption']");
        assertEquals(1, gradleRadio.count(), "Should have Gradle build tool radio button");

    }

    @Test
    @DisplayName("Configurator should have feature checkboxes for all enabled flags")
    void testIndexHasFeatureCheckboxes() {
        navigateToIndex();

        VariantsConfig config = VariantsConfig.load();
        for (VariantsConfig.Flag flag : config.enabledFlags()) {
            Locator checkbox = page.locator("input[id='use-" + flag.id() + "']");
            assertTrue(checkbox.count() > 0, "Should have checkbox for " + flag.id());
        }
    }

    @Test
    @DisplayName("Selecting a standalone module should uncheck the other standalone modules")
    void testStandaloneModulesAreMutuallyExclusive() {
        navigateToIndex();

        VariantsConfig config = VariantsConfig.load();
        List<VariantsConfig.Flag> standalone = config.standaloneFlags();
        Assumptions.assumeTrue(standalone.size() >= 2,
            "Need at least 2 standalone flags to test mutual exclusion");

        VariantsConfig.Flag first = standalone.get(0);
        VariantsConfig.Flag second = standalone.get(1);

        // Check the first standalone flag
        page.locator("input[id='use-" + first.id() + "']").check();
        assertTrue(page.locator("input[id='use-" + first.id() + "']").isChecked(),
            first.id() + " should be checked");

        // Check the second standalone flag
        page.locator("input[id='use-" + second.id() + "']").check();
        assertTrue(page.locator("input[id='use-" + second.id() + "']").isChecked(),
            second.id() + " should be checked");

        // The first should now be unchecked
        assertFalse(page.locator("input[id='use-" + first.id() + "']").isChecked(),
            first.id() + " should be unchecked after selecting " + second.id());
    }

    @Test
    @DisplayName("Select all button should check all enabled flags")
    void testSelectAllButtonChecksEverything() {
        navigateToIndex();

        // First uncheck everything to start from a clean state
        VariantsConfig config = VariantsConfig.load();
        for (VariantsConfig.Flag flag : config.enabledFlags()) {
            Locator cb = page.locator("input[id='use-" + flag.id() + "']");
            if (cb.isChecked()) {
                cb.uncheck();
            }
        }

        // Click "Select all"
        page.locator("a:has-text('Select all')").click();

        // Every enabled flag should now be checked
        for (VariantsConfig.Flag flag : config.enabledFlags()) {
            Locator cb = page.locator("input[id='use-" + flag.id() + "']");
            assertTrue(cb.isChecked(),
                flag.id() + " should be checked after Select all");
            assertFalse(cb.isDisabled(),
                flag.id() + " should not be disabled after Select all");
        }
    }

    @Test
    @DisplayName("Select all button should re-enable and check everything even after standalone selection")
    void testSelectAllAfterStandaloneSelection() {
        navigateToIndex();

        VariantsConfig config = VariantsConfig.load();
        List<VariantsConfig.Flag> standalone = config.standaloneFlags();
        Assumptions.assumeTrue(!standalone.isEmpty(),
            "Need at least 1 standalone flag");

        // Check a standalone flag — this disables non-standalone checkboxes
        page.locator("input[id='use-" + standalone.get(0).id() + "']").check();

        // Verify non-standalone flags are disabled
        for (VariantsConfig.Flag flag : config.enabledNonStandaloneFlags()) {
            assertTrue(page.locator("input[id='use-" + flag.id() + "']").isDisabled(),
                flag.id() + " should be disabled after standalone selection");
        }

        // Click "Select all"
        page.locator("a:has-text('Select all')").click();

        // Every enabled flag should now be checked and enabled
        for (VariantsConfig.Flag flag : config.enabledFlags()) {
            Locator cb = page.locator("input[id='use-" + flag.id() + "']");
            assertTrue(cb.isChecked(),
                flag.id() + " should be checked after Select all");
            assertFalse(cb.isDisabled(),
                flag.id() + " should not be disabled after Select all");
        }
    }

    @Test
    @DisplayName("Select all then custom workshop should navigate to valid everything variant")
    void testSelectAllThenNavigate() {
        Path variantsDir = new File(DOCS_BASE_PATH, "variants").toPath();
        Assumptions.assumeTrue(Files.isDirectory(variantsDir),
            "Variants directory does not exist yet (generated during package phase)");

        navigateToIndex();

        // Click "Select all"
        page.locator("a:has-text('Select all')").click();

        // Click "Take me to my custom workshop"
        Locator customButton = page.locator("button:has-text('Take me to my custom workshop')");
        page.waitForLoadState();
        customButton.click();
        page.waitForLoadState();

        // Verify we navigated to a valid page
        String currentUrl = page.url();
        assertFalse(currentUrl.startsWith("chrome-error:"),
            "Page should load without error, but got: " + currentUrl);
        assertFalse(currentUrl.contains("index.html"),
            "Should have navigated away from index.html");
        assertTrue(currentUrl.contains("bt-"),
            "URL should include build tool prefix bt-: " + currentUrl);
        assertTrue(currentUrl.contains("contract-testing-true"),
            "URL should include contract-testing-true: " + currentUrl);
        assertTrue(currentUrl.contains("extension-true"),
            "URL should include extension-true: " + currentUrl);

        // Verify the page has content
        String title = page.title();
        assertNotNull(title, "Page should have a title");
        assertTrue(title.toLowerCase().contains("quarkus") || title.toLowerCase().contains("workshop"),
            "Title should contain 'Quarkus' or 'Workshop', but was: " + title);
    }

    @Test
    @DisplayName("Select all button should re-enable standalone flags after non-standalone selection")
    void testSelectAllAfterNonStandaloneSelection() {
        navigateToIndex();

        VariantsConfig config = VariantsConfig.load();
        List<VariantsConfig.Flag> nonStandalone = config.enabledNonStandaloneFlags();
        Assumptions.assumeTrue(!nonStandalone.isEmpty(),
            "Need at least 1 non-standalone flag");

        // Check a non-standalone flag — this disables standalone checkboxes
        page.locator("input[id='use-" + nonStandalone.get(0).id() + "']").check();

        // Verify standalone flags are disabled
        for (VariantsConfig.Flag flag : config.standaloneFlags()) {
            assertTrue(page.locator("input[id='use-" + flag.id() + "']").isDisabled(),
                flag.id() + " should be disabled after non-standalone selection");
        }

        // Click "Select all"
        page.locator("a:has-text('Select all')").click();

        // Every enabled flag should now be checked and enabled
        for (VariantsConfig.Flag flag : config.enabledFlags()) {
            Locator cb = page.locator("input[id='use-" + flag.id() + "']");
            assertTrue(cb.isChecked(),
                flag.id() + " should be checked after Select all");
            assertFalse(cb.isDisabled(),
                flag.id() + " should not be disabled after Select all");
        }
    }

    @Test
    @DisplayName("None link should uncheck all enabled flags")
    void testNoneUnchecksEverything() {
        navigateToIndex();

        // First select all so everything is checked
        page.locator("a:has-text('Select all')").click();

        // Click "none"
        page.locator("a:has-text('none')").click();

        // Every enabled flag should now be unchecked and enabled
        VariantsConfig config = VariantsConfig.load();
        for (VariantsConfig.Flag flag : config.enabledFlags()) {
            Locator cb = page.locator("input[id='use-" + flag.id() + "']");
            assertFalse(cb.isChecked(),
                flag.id() + " should be unchecked after none");
            assertFalse(cb.isDisabled(),
                flag.id() + " should not be disabled after none");
        }
    }

    @Test
    @DisplayName("None link should re-enable all flags after standalone selection")
    void testNoneAfterStandaloneSelection() {
        navigateToIndex();

        VariantsConfig config = VariantsConfig.load();
        List<VariantsConfig.Flag> standalone = config.standaloneFlags();
        Assumptions.assumeTrue(!standalone.isEmpty(),
            "Need at least 1 standalone flag");

        // Check a standalone flag — this disables non-standalone checkboxes
        page.locator("input[id='use-" + standalone.get(0).id() + "']").check();

        // Click "none"
        page.locator("a:has-text('none')").click();

        // Every enabled flag should now be unchecked and enabled
        for (VariantsConfig.Flag flag : config.enabledFlags()) {
            Locator cb = page.locator("input[id='use-" + flag.id() + "']");
            assertFalse(cb.isChecked(),
                flag.id() + " should be unchecked after none");
            assertFalse(cb.isDisabled(),
                flag.id() + " should not be disabled after none");
        }
    }

    @Test
    @DisplayName("spine.adoc should have ifdef blocks for every enabled flag")
    void testSpineAdocHasAllFlags() throws Exception {
        Path spineFile = Path.of(DOCS_BASE_PATH.getParent(), "src/docs/asciidoc/spine.adoc");
        if (!Files.exists(spineFile)) {
            spineFile = Path.of("src/docs/asciidoc/spine.adoc");
        }
        Assumptions.assumeTrue(Files.exists(spineFile), "spine.adoc not found");

        String spineContent = Files.readString(spineFile);
        VariantsConfig config = VariantsConfig.load();

        for (VariantsConfig.Flag flag : config.enabledFlags()) {
            assertTrue(spineContent.contains("ifdef::use-" + flag.id() + "[]"),
                "spine.adoc should have ifdef::use-" + flag.id() + "[] block");
        }
    }

    @ParameterizedTest
    @MethodSource("generateAllCombinations")
    @DisplayName("All configurator combinations should resolve to valid pages")
    void testAllCombinationsResolveToValidPages(CombinationParams params) {
        Path variantsDir = new File(DOCS_BASE_PATH, "variants").toPath();
        Assumptions.assumeTrue(Files.isDirectory(variantsDir),
            "Variants directory does not exist yet (generated during package phase)");

        navigateToIndex();

        // Select the build tool radio button
        String btRadioId = switch (params.buildTool) {
            case "maven" -> "mavenRadio";
            case "gradle" -> "gradleRadio";
            default -> "mavenRadio";
        };
        Locator btRadio = page.locator("input[id='" + btRadioId + "']");
        if (btRadio.count() > 0) {
            btRadio.check();
        }

        // Select the OS radio button
        String osRadioId = params.os + "Radio";
        Locator osRadio = page.locator("input[id='" + osRadioId + "']");
        if (osRadio.count() > 0) {
            osRadio.check();
        }

        // Set all the feature checkboxes according to params
        VariantsConfig config = VariantsConfig.load();
        for (VariantsConfig.Flag flag : config.enabledFlags()) {
            setCheckbox("use-" + flag.id(), params.flags.getOrDefault(flag.id(), false));
        }

        // Click the "Take me to my custom workshop" button
        Locator customButton = page.locator("button:has-text('Take me to my custom workshop')");

        page.waitForLoadState();
        customButton.click();
        page.waitForLoadState();

        // Verify we navigated to a valid page
        String currentUrl = page.url();
        assertFalse(currentUrl.contains("index.html"),
            "Should have navigated away from index.html for: " + params);
        assertTrue(currentUrl.contains("bt-" + params.buildTool),
            "URL should include bt-" + params.buildTool + " for: " + params + ", but was: " + currentUrl);

        // Verify the page has basic structure
        String title = page.title();
        assertNotNull(title, "Page should have a title for: " + params);
        assertFalse(title.isEmpty(), "Title should not be empty for: " + params);
        assertTrue(title.toLowerCase().contains("quarkus") || title.toLowerCase().contains("workshop"),
            "Title should contain 'Quarkus' or 'Workshop' for: " + params + ", but was: " + title);

        // Verify it has content
        Locator body = page.locator("body");
        assertTrue(body.count() > 0, "Page should have body content for: " + params);
    }

    private void setCheckbox(String checkboxId, boolean checked) {
        // Use JS evaluation to bypass constraint enforcement that may have disabled checkboxes.
        // The test needs to set arbitrary valid combinations without the UI's mutual-exclusion
        // logic interfering.
        page.evaluate("([id, val]) => { const cb = document.getElementById(id); if (cb) { cb.disabled = false; cb.checked = val; } }",
            new Object[]{checkboxId, checked});
    }

    private static String[] getBuildToolOptionsToTest(String[] allBtOptions) {
        String btProperty = System.getProperty("buildTool");
        if (btProperty != null && !btProperty.isEmpty()) {
            for (String validBt : allBtOptions) {
                if (validBt.equals(btProperty)) {
                    return new String[]{btProperty};
                }
            }
            System.err.println("Warning: Unknown buildTool property value '" + btProperty +
                "', testing all build tool options");
        }
        return allBtOptions;
    }

    private static String[] getOsOptionsToTest(String[] allOsOptions) {
        String osProperty = System.getProperty("os");
        if (osProperty != null && !osProperty.isEmpty()) {
            for (String validOs : allOsOptions) {
                if (validOs.equals(osProperty)) {
                    return new String[]{osProperty};
                }
            }
            System.err.println("Warning: Unknown OS property value '" + osProperty +
                "', testing all OS options");
        }
        return allOsOptions;
    }

    static Stream<CombinationParams> generateAllCombinations() {
        VariantsConfig config = VariantsConfig.load();
        List<CombinationParams> combinations = new ArrayList<>();

        for (String bt : BT_OPTIONS) {
            for (String os : OS_OPTIONS) {
                for (Map<String, Boolean> assignment : config.generateValidCombinations(os)) {
                    combinations.add(new CombinationParams(os, bt, assignment));
                }
            }
        }

        return combinations.stream();
    }

    private void navigateToIndex() {
        navigateTo(new File(DOCS_BASE_PATH, INDEX_HTML).toPath());
    }

    static class CombinationParams {
        final String os;
        final String buildTool;
        final Map<String, Boolean> flags;

        CombinationParams(String os, String buildTool, Map<String, Boolean> flags) {
            this.os = os;
            this.buildTool = buildTool;
            this.flags = flags;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("bt=").append(buildTool).append(", os=").append(os);
            flags.forEach((k, v) -> sb.append(", ").append(k).append("=").append(v));
            return sb.toString();
        }
    }
}
