package io.quarkus.workshop.docs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
 */
public class ConfiguratorTest extends DocumentationTestBase {

    private static final String INDEX_HTML = "index.html";

    private static final String[] ALL_OS_OPTIONS = {"all", "mac", "linux", "windows"};

    // Allow filtering by OS via system property (matches the -Dos=<value> build parameter)
    private static final String[] OS_OPTIONS = getOsOptionsToTest();

    // Based on generate-attribute-combinations.sh, some flags are disabled
    private static final boolean[] AI_VALUES = {true, false};
    private static final boolean[] AZURE_VALUES = {true, false};
    private static final boolean[] CLI_VALUES = {false}; // disabled
    private static final boolean[] CONTAINER_VALUES = {true, false};
    private static final boolean[] CONTRACT_TESTING_VALUES = {true, false};
    private static final boolean[] EXTENSION_VALUES = {true, false};
    private static final boolean[] KUBERNETES_VALUES = {true, false};
    private static final boolean[] MESSAGING_VALUES = {true, false};
    private static final boolean[] NATIVE_VALUES = {true, false};
    private static final boolean[] OBSERVABILITY_VALUES = {false}; // disabled

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
    @DisplayName("Configurator should have OS selection options")
    void testIndexHasOsOptions() {
        navigateToIndex();

        for (String os : new String[]{"mac", "windows", "linux"}) {
            Locator osRadio = page.locator("input[id='" + os + "Radio']");
            assertEquals(1, osRadio.count(), "Should have " + os + " radio button");
        }
    }

    @Test
    @DisplayName("Configurator should have feature checkboxes")
    void testIndexHasFeatureCheckboxes() {
        navigateToIndex();

        // Check for visible feature checkboxes (cli and observability are disabled)
        String[] visibleFlags = {"ai", "azure", "container", "contract-testing",
            "extension", "kubernetes", "messaging", "native"};

        for (String flag : visibleFlags) {
            Locator checkbox = page.locator("input[id='use-" + flag + "']");
            assertTrue(checkbox.count() > 0, "Should have checkbox for " + flag);
        }
    }

    @ParameterizedTest
    @MethodSource("generateAllCombinations")
    @DisplayName("All configurator combinations should resolve to valid pages")
    void testAllCombinationsResolveToValidPages(CombinationParams params) {
        Path variantsDir = new File(DOCS_BASE_PATH, "variants").toPath();
        Assumptions.assumeTrue(Files.isDirectory(variantsDir),
            "Variants directory does not exist yet (generated during package phase)");

        // Navigate to the configurator page
        navigateToIndex();

        // Select the OS radio button
        String osRadioId = params.os + "Radio";
        Locator osRadio = page.locator("input[id='" + osRadioId + "']");
        if (osRadio.count() > 0) {
            osRadio.check();
        }

        // Set all the feature checkboxes according to params
        setCheckbox("use-ai", params.ai);
        setCheckbox("use-azure", params.azure);
        setCheckbox("use-cli", params.cli);
        setCheckbox("use-container", params.container);
        setCheckbox("use-contract-testing", params.contractTesting);
        setCheckbox("use-extension", params.extension);
        setCheckbox("use-kubernetes", params.kubernetes);
        setCheckbox("use-messaging", params.messaging);
        setCheckbox("use-native", params.nativeComp);
        setCheckbox("use-observability", params.observability);

        // Click the "Take me to my custom workshop" button
        Locator customButton = page.locator("button:has-text('Take me to my custom workshop')");

        // Wait for navigation to complete
        page.waitForLoadState();
        customButton.click();
        page.waitForLoadState();

        // Verify we navigated to a valid page
        String currentUrl = page.url();
        assertFalse(currentUrl.contains("index.html"),
            "Should have navigated away from index.html for: " + params);

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
        Locator checkbox = page.locator("input[id='" + checkboxId + "']");
        if (checkbox.count() > 0) {
            if (checked) {
                checkbox.check();
            } else {
                checkbox.uncheck();
            }
        }
    }

    /**
     * Determines which OS options to test based on system property.
     * If -Dos=<value> is set, only test that OS. Otherwise test all.
     */
    private static String[] getOsOptionsToTest() {
        String osProperty = System.getProperty("os");
        if (osProperty!=null && !osProperty.isEmpty()) {
            // Validate it's a known OS
            for (String validOs : ALL_OS_OPTIONS) {
                if (validOs.equals(osProperty)) {
                    return new String[]{osProperty};
                }
            }
            System.err.println("Warning: Unknown OS property value '" + osProperty +
                "', testing all OS options");
        }
        return ALL_OS_OPTIONS;
    }

    /**
     * Generates all possible combinations based on the configurator logic
     */
    static Stream<CombinationParams> generateAllCombinations() {
        List<CombinationParams> combinations = new ArrayList<>();

        for (String os : OS_OPTIONS) {
            for (boolean ai : AI_VALUES) {
                for (boolean azure : AZURE_VALUES) {
                    for (boolean cli : CLI_VALUES) {
                        for (boolean container : CONTAINER_VALUES) {
                            for (boolean contractTesting : CONTRACT_TESTING_VALUES) {
                                for (boolean extension : EXTENSION_VALUES) {
                                    for (boolean kubernetes : KUBERNETES_VALUES) {
                                        for (boolean messaging : MESSAGING_VALUES) {
                                            for (boolean nativeComp : NATIVE_VALUES) {
                                                for (boolean observability : OBSERVABILITY_VALUES) {
                                                    combinations.add(new CombinationParams(
                                                        os, ai, azure, cli, container, contractTesting,
                                                        extension, kubernetes, messaging, nativeComp, observability
                                                    ));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return combinations.stream();
    }

    private void navigateToIndex() {
        navigateTo(new File(DOCS_BASE_PATH, INDEX_HTML).toPath());
    }

    /**
     * Parameter class to hold a combination of configuration options
     */
    static class CombinationParams {
        final String os;
        final boolean ai;
        final boolean azure;
        final boolean cli;
        final boolean container;
        final boolean contractTesting;
        final boolean extension;
        final boolean kubernetes;
        final boolean messaging;
        final boolean nativeComp;
        final boolean observability;

        CombinationParams(String os, boolean ai, boolean azure, boolean cli,
                          boolean container, boolean contractTesting, boolean extension,
                          boolean kubernetes, boolean messaging, boolean nativeComp,
                          boolean observability) {
            this.os = os;
            this.ai = ai;
            this.azure = azure;
            this.cli = cli;
            this.container = container;
            this.contractTesting = contractTesting;
            this.extension = extension;
            this.kubernetes = kubernetes;
            this.messaging = messaging;
            this.nativeComp = nativeComp;
            this.observability = observability;
        }

        @Override
        public String toString() {
            return String.format("os=%s, ai=%s, azure=%s, cli=%s, container=%s, contract-testing=%s, " +
                    "extension=%s, kubernetes=%s, messaging=%s, native=%s, observability=%s",
                os, ai, azure, cli, container, contractTesting, extension, kubernetes,
                messaging, nativeComp, observability);
        }
    }
}
