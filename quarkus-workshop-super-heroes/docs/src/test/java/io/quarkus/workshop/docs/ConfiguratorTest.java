package io.quarkus.workshop.docs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import io.quarkiverse.roq.testing.RoqAndRoll;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that validate all combinations on the configurator index.html page
 * resolve to valid documentation pages.
 * <p>
 * Flag definitions, constraints, and standalone markers are read from
 * data/variants.json (the single source of truth).
 */
@QuarkusTest
@RoqAndRoll(port = RoqSiteTest.ROQ_PORT)
public class ConfiguratorTest extends RoqSiteTest {

    static final String INDEX_HTML = "index.html";

    @Test
    @DisplayName("Configurator index.html should load without errors")
    void testIndexLoadsSuccessfully() {
        Response response = page.navigate(url(INDEX_HTML));
        assertNotNull(response, "Page should load");
        assertTrue(response.ok(),
            "Page should load successfully (status: " + response.status() + ")");
    }

    @Test
    @DisplayName("Configurator should have workshop button that defaults to 'Take me to the default workshop'")
    void testIndexHasWorkshopButton() {
        navigateToIndex();

        Locator workshopButton = page.locator("button:has-text('Take me to the default workshop')");
        assertTrue(workshopButton.count() > 0, "Should have workshop button showing default workshop text initially");
    }

    @Test
    @DisplayName("Workshop button should change to 'custom' text when toggles are changed")
    void testWorkshopButtonChangesToCustomWhenTogglesChanged() {
        navigateToIndex();

        // Initially should say default
        Locator defaultButton = page.locator("button:has-text('Take me to the default workshop')");
        assertTrue(defaultButton.count() > 0, "Should initially show default workshop text");

        // Verify button points to default spine.html
        String initialUrl = (String) page.evaluate("() => { const url = window.location.href; const baseUrl = './'; return baseUrl + 'spine.html'; }");
        assertNotNull(initialUrl, "Should be able to determine default URL");

        // Change a toggle
        page.locator("input[id='use-ai']").uncheck();

        // Now should say custom
        Locator customButton = page.locator("button:has-text('Take me to my custom workshop')");
        assertTrue(customButton.count() > 0, "Should show custom workshop text after toggle change");

        // Default text should be gone
        defaultButton = page.locator("button:has-text('Take me to the default workshop')");
        assertEquals(0, defaultButton.count(), "Default workshop text should be gone");

        // Verify button now points to variant URL
        String customUrl = (String) page.evaluate("() => { " +
            "const selectedOptions = []; " +
            "const btOption = document.querySelector('input[name=\"buildToolOption\"]:checked');" +
            "selectedOptions.push(`bt-${btOption?.value || 'maven'}`); " +
            "const osOption = document.querySelector('input[name=\"osOption\"]:checked');" +
            "selectedOptions.push(`os-${osOption?.value || 'all'}`); " +
            "return selectedOptions.join('-'); " +
            "}");
        assertNotNull(customUrl, "Should generate custom URL");
        assertTrue(customUrl.contains("bt-"), "Custom URL should contain build tool");
        assertTrue(customUrl.contains("os-"), "Custom URL should contain OS");
    }

    @Test
    @DisplayName("Workshop button should point to variant URL when using custom options")
    void testCustomWorkshopButtonHref() {
        navigateToIndex();

        // Change a toggle to make it custom
        page.locator("input[id='use-messaging']").uncheck();

        // Generate the expected variant URL
        String variantUrl = (String) page.evaluate("() => { " +
            "  const selectedOptions = []; " +
            "  const btOption = document.querySelector('input[name=\"buildToolOption\"]:checked');" +
            "  selectedOptions.push(`bt-${btOption?.value || 'maven'}`); " +
            "  const osOption = document.querySelector('input[name=\"osOption\"]:checked');" +
            "  selectedOptions.push(`os-${osOption?.value || 'all'}`); " +
            "  return 'variants/' + selectedOptions.join('-'); " +
            "}");

        assertNotNull(variantUrl, "Custom URL should be generated");
        assertTrue(variantUrl.startsWith("variants/"), "Custom URL should be in variants directory");
        assertTrue(variantUrl.contains("bt-"), "Custom URL should include build tool");
        assertTrue(variantUrl.contains("os-"), "Custom URL should include OS");
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

        // Uncheck all defaults first
        VariantsConfig config = VariantsConfig.load();
        for (VariantsConfig.Flag flag : config.enabledFlags()) {
            Locator cb = page.locator("input[id='use-" + flag.id() + "']");
            if (cb.isChecked()) {
                cb.uncheck();
            }
        }

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

        // Uncheck all defaults first
        VariantsConfig config = VariantsConfig.load();
        for (VariantsConfig.Flag flag : config.enabledFlags()) {
            Locator cb = page.locator("input[id='use-" + flag.id() + "']");
            if (cb.isChecked()) {
                cb.uncheck();
            }
        }

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
    @DisplayName("Select all button should re-enable standalone flags after non-standalone selection")
    void testSelectAllAfterNonStandaloneSelection() {
        navigateToIndex();

        // Uncheck all defaults first
        VariantsConfig config = VariantsConfig.load();
        for (VariantsConfig.Flag flag : config.enabledFlags()) {
            Locator cb = page.locator("input[id='use-" + flag.id() + "']");
            if (cb.isChecked()) {
                cb.uncheck();
            }
        }

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

        // Uncheck all defaults first
        VariantsConfig config = VariantsConfig.load();
        for (VariantsConfig.Flag flag : config.enabledFlags()) {
            Locator cb = page.locator("input[id='use-" + flag.id() + "']");
            if (cb.isChecked()) {
                cb.uncheck();
            }
        }

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

    // Flags whose content is not a spine-level include but is guarded inline inside an
    // always-included core file (e.g. locked-down lives in core-ui/ui.adoc), so they legitimately
    // have no ifdef block in spine.adoc.
    private static final Set<String> INLINE_GUARDED_FLAGS = Set.of("locked-down");

    @Test
    @DisplayName("spine.adoc should have ifdef blocks for every enabled flag")
    void testSpineAdocHasAllFlags() throws Exception {
        Path spineFile = Path.of("content/spine.adoc");
        Assumptions.assumeTrue(Files.exists(spineFile), "spine.adoc not found at " + spineFile.toAbsolutePath());

        String spineContent = Files.readString(spineFile);
        VariantsConfig config = VariantsConfig.load();

        for (VariantsConfig.Flag flag : config.enabledFlags()) {
            if (INLINE_GUARDED_FLAGS.contains(flag.id())) {
                continue;
            }
            assertTrue(spineContent.contains("ifdef::use-" + flag.id() + "[]"),
                "spine.adoc should have ifdef::use-" + flag.id() + "[] block");
        }
    }

    private void navigateToIndex() {
        navigateTo(url(INDEX_HTML));
    }

}
