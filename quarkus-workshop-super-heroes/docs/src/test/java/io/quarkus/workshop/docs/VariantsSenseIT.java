package io.quarkus.workshop.docs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for the variant spine/index.htmls that don't rely on Playwright.
 */
public class VariantsSenseIT {

    private static final File VARIANTS_PATH = new File(
        System.getProperty("docs.base.path", "target/roq/"), "variants");

    @Test
    @DisplayName("Sample of variants should have different content based on flags")
    void variantsShouldHaveDifferentContent() throws IOException {
        Path variantsDir = VARIANTS_PATH.toPath();
        assumeTrue(Files.exists(variantsDir), "Variants directory not generated yet");

        List<Path> variantFiles = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(variantsDir, 2)) {
            paths.filter(Files::isDirectory)
                .filter(p -> !p.equals(variantsDir))
                .limit(5)
                .forEach(dir -> {
                    Path spineFile = dir.resolve("spine/index.html");
                    if (Files.exists(spineFile)) {
                        variantFiles.add(spineFile);
                    }
                });
        }

        assumeTrue(variantFiles.size() >= 2, "Need at least 2 variants to compare");

        Set<Long> fileSizes = new HashSet<>();
        for (Path variant : variantFiles) {
            fileSizes.add(Files.size(variant));
        }

        if (variantFiles.size() > 2) {
            assertTrue(fileSizes.size() > 1,
                "Variants should have different content sizes (found " + fileSizes.size() +
                    " unique sizes from " + variantFiles.size() + " variants)");
        }
    }

    @Test
    @DisplayName("Maven and Gradle variants of same flags should have different build tool content")
    void buildToolVariantsShouldDiffer() throws IOException {
        Path variantsDir = VARIANTS_PATH.toPath();
        assumeTrue(Files.exists(variantsDir), "Variants directory not generated yet");

        // Find a matching pair: same flags, different build tool
        List<Path> mavenVariants = new ArrayList<>();
        List<Path> gradleVariants = new ArrayList<>();
        try (Stream<Path> paths = Files.list(variantsDir)) {
            paths.filter(Files::isDirectory).forEach(dir -> {
                String name = dir.getFileName().toString();
                if (name.startsWith("bt-maven-")) {
                    mavenVariants.add(dir);
                } else if (name.startsWith("bt-gradle-")) {
                    gradleVariants.add(dir);
                }
            });
        }

        assumeTrue(!mavenVariants.isEmpty() && !gradleVariants.isEmpty(),
            "Need both bt-maven and bt-gradle variants");

        // Find a matching pair by comparing the suffix after the bt- prefix
        Path mavenSpine = null;
        Path gradleSpine = null;
        for (Path mv : mavenVariants) {
            String suffix = mv.getFileName().toString().substring("bt-maven-".length());
            for (Path gv : gradleVariants) {
                if (gv.getFileName().toString().substring("bt-gradle-".length()).equals(suffix)) {
                    Path ms = mv.resolve("spine/index.html");
                    Path gs = gv.resolve("spine/index.html");
                    if (Files.exists(ms) && Files.exists(gs)) {
                        mavenSpine = ms;
                        gradleSpine = gs;
                        break;
                    }
                }
            }
            if (mavenSpine!=null) break;
        }

        assumeTrue(mavenSpine!=null, "No matching maven/gradle variant pair found");

        String mavenContent = Files.readString(mavenSpine);
        String gradleContent = Files.readString(gradleSpine);

        assertTrue(mavenContent.contains("mvnw"),
            "Maven variant should contain 'mvnw'");
        assertFalse(mavenContent.contains("gradlew"),
            "Maven variant should not contain 'gradlew'");

        assertTrue(gradleContent.contains("gradlew"),
            "Gradle variant should contain 'gradlew'");
    }

    @Test
    @DisplayName("Locked-down variants should download from codeload, and the others from the archive url")
    void downloadLinksShouldDependOnLockedDown() throws IOException {
        String lockedDown = Files.readString(findSpine("locked-down-true"));
        String open = Files.readString(findSpine("locked-down-false"));

        assertTrue(lockedDown.contains("https://codeload.github.com/"),
            "Locked-down variant should link to codeload.github.com");
        assertFalse(lockedDown.contains("/archive/refs/heads/"),
            "Locked-down variant should not link to the redirecting archive url");

        assertTrue(open.contains("/archive/refs/heads/download/"),
            "Variant that is not locked down should link to the archive url");
        assertFalse(open.contains("codeload.github.com"),
            "Variant that is not locked down should not link to codeload.github.com");
    }

    @Test
    @DisplayName("Variants should explain how to clone the code, as well as how to download it")
    void downloadsShouldAlsoBeClonable() throws IOException {
        for (String lockedDown : List.of("locked-down-true", "locked-down-false")) {
            String spine = Files.readString(findSpine(lockedDown));
            assertTrue(spine.contains("git clone --branch download/start --single-branch "),
                lockedDown + " variant should say how to clone the start code");
            assertTrue(spine.contains("git clone --branch download/complete --single-branch "),
                lockedDown + " variant should say how to clone the complete code");
        }
    }

    private static Path findSpine(String mustContain) throws IOException {
        Path variantsDir = VARIANTS_PATH.toPath();
        assumeTrue(Files.exists(variantsDir), "Variants directory not generated yet");

        try (Stream<Path> paths = Files.list(variantsDir)) {
            Path spine = paths.filter(dir -> dir.getFileName().toString().contains(mustContain))
                .map(dir -> dir.resolve("spine/index.html"))
                .filter(Files::exists)
                .findFirst()
                .orElse(null);
            assumeTrue(spine!=null, "No " + mustContain + " variant found");
            return spine;
        }
    }
}
