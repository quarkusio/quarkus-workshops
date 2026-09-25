package io.quarkus.workshop.docs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests that verify build tool variant content and tab behaviour.
 * Requires variants to be generated (run {@code mvn package} first).
 */
public class BuildToolTest extends DocumentationTestBase {

    private static final File VARIANTS_PATH = new File(DOCS_BASE_PATH, "variants");

    private Path findVariant(String btPrefix) throws IOException {
        return findVariant(btPrefix, null);
    }

    private Path findVariant(String btPrefix, String mustContain) throws IOException {
        Path variantsDir = VARIANTS_PATH.toPath();
        assumeTrue(Files.isDirectory(variantsDir), "Variants directory not generated yet");

        try (Stream<Path> paths = Files.list(variantsDir)) {
            return paths.filter(Files::isDirectory)
                .filter(p -> {
                    String name = p.getFileName().toString();
                    if (!name.startsWith(btPrefix)) return false;
                    if (mustContain != null && !name.contains(mustContain)) return false;
                    return true;
                })
                .map(p -> p.resolve(SPINE_HTML))
                .filter(Files::exists)
                .findFirst()
                .orElse(null);
        }
    }

    @Test
    @DisplayName("Maven-only variant should not contain Gradle commands in code blocks")
    void mavenOnlyVariantShouldNotContainGradleCommands() throws IOException {
        Path spine = findVariant("bt-maven-");
        assumeTrue(spine != null, "No bt-maven variant found");

        navigateTo(spine);

        Locator codeBlocks = page.locator("pre code");
        int count = codeBlocks.count();
        for (int i = 0; i < count; i++) {
            String text = codeBlocks.nth(i).textContent();
            assertFalse(text.contains("gradlew"),
                "Maven-only variant should not have 'gradlew' in code block: " + text.substring(0, Math.min(80, text.length())));
            assertFalse(text.contains("quarkusDev"),
                "Maven-only variant should not have 'quarkusDev' in code block: " + text.substring(0, Math.min(80, text.length())));
            assertFalse(text.contains("addExtension"),
                "Maven-only variant should not have 'addExtension' in code block: " + text.substring(0, Math.min(80, text.length())));
        }
    }

    @Test
    @DisplayName("Maven-only variant should contain Maven commands")
    void mavenOnlyVariantShouldContainMavenCommands() throws IOException {
        Path spine = findVariant("bt-maven-");
        assumeTrue(spine != null, "No bt-maven variant found");

        navigateTo(spine);

        String allCode = page.locator("pre code").allTextContents().stream()
            .reduce("", String::concat);
        assertTrue(allCode.contains("mvnw"), "Maven-only variant should contain 'mvnw' commands");
    }

    @Test
    @DisplayName("Gradle-only variant should not contain Maven commands in code blocks")
    void gradleOnlyVariantShouldNotContainMavenCommands() throws IOException {
        // Exclude extension variants — the extension chapter legitimately uses ./mvnw
        Path spine = findVariant("bt-gradle-", "extension-false");
        assumeTrue(spine != null, "No bt-gradle variant without extension found");

        navigateTo(spine);

        Locator codeBlocks = page.locator("pre code");
        int count = codeBlocks.count();
        for (int i = 0; i < count; i++) {
            String text = codeBlocks.nth(i).textContent();
            assertFalse(text.contains("./mvnw"),
                "Gradle-only variant should not have './mvnw' in code block: " + text.substring(0, Math.min(80, text.length())));
            assertFalse(text.contains("quarkus:dev"),
                "Gradle-only variant should not have 'quarkus:dev' in code block: " + text.substring(0, Math.min(80, text.length())));
            assertFalse(text.contains("quarkus:add-extension"),
                "Gradle-only variant should not have 'quarkus:add-extension' in code block: " + text.substring(0, Math.min(80, text.length())));
        }
    }

    @Test
    @DisplayName("Gradle-only variant should contain Gradle commands")
    void gradleOnlyVariantShouldContainGradleCommands() throws IOException {
        Path spine = findVariant("bt-gradle-");
        assumeTrue(spine != null, "No bt-gradle variant found");

        navigateTo(spine);

        String allCode = page.locator("pre code").allTextContents().stream()
            .reduce("", String::concat);
        assertTrue(allCode.contains("gradlew"), "Gradle-only variant should contain 'gradlew' commands");
    }

    @Test
    @DisplayName("Maven-only variant should not have tab containers")
    void mavenOnlyVariantShouldNotHaveTabs() throws IOException {
        Path spine = findVariant("bt-maven-");
        assumeTrue(spine != null, "No bt-maven variant found");

        navigateTo(spine);

        Locator tabs = page.locator(".buildtool-tabs");
        assertEquals(0, tabs.count(), "Maven-only variant should not have tab containers");
    }

    @Test
    @DisplayName("Gradle-only variant should not have tab containers")
    void gradleOnlyVariantShouldNotHaveTabs() throws IOException {
        Path spine = findVariant("bt-gradle-");
        assumeTrue(spine != null, "No bt-gradle variant found");

        navigateTo(spine);

        Locator tabs = page.locator(".buildtool-tabs");
        assertEquals(0, tabs.count(), "Gradle-only variant should not have tab containers");
    }
}
