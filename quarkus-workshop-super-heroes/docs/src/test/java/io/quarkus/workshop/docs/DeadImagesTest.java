package io.quarkus.workshop.docs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import io.quarkiverse.roq.testing.RoqAndRoll;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks every page of the site for images that do not load.
 * <p>
 * The other tests only look at the spine, which hides a whole class of problem: the standalone
 * chapter pages sit one directory deeper than the spine does, so an {@code imagesdir} that is
 * right for one is wrong for the other, and nothing noticed because the images were never
 * fetched over http.
 * <p>
 * Variant pages are left to {@link VariantsIT}; this is about the canonical site.
 */
@QuarkusTest
@RoqAndRoll(port = RoqSiteTest.ROQ_PORT)
public class DeadImagesTest extends RoqSiteTest {

    // A @TestFactory rather than a @ParameterizedTest because the pages only exist once Roq has
    // generated them, which happens after the test instance is constructed. A @MethodSource would
    // be asked for its arguments before that, and would silently contribute no tests at all.
    @TestFactory
    Stream<DynamicTest> everyPageShouldHaveLiveImages() throws IOException {
        Path root = outputDir();
        return pagesUnder(root).stream()
            .map(pageFile -> DynamicTest.dynamicTest(root.relativize(pageFile).toString(), () -> {
                navigateTo(url(relativeUrl(root, pageFile)));
                Set<String> dead = findDeadImages();
                assertTrue(dead.isEmpty(),
                    root.relativize(pageFile) + " has " + dead.size() + " dead image(s): "
                        + String.join(", ", dead));
            }));
    }

    @TestFactory
    Stream<DynamicTest> everyPageShouldHaveResolvedGitHubUrls() throws IOException {
        Path root = outputDir();
        return pagesUnder(root).stream()
            .map(pageFile -> DynamicTest.dynamicTest(
                "GitHub URLs resolved in " + root.relativize(pageFile).toString(), () -> {
                    String content = Files.readString(pageFile);
                    assertFalse(content.contains("[server]"),
                        root.relativize(pageFile) + " contains unresolved [server] placeholder");
                    assertFalse(content.contains("[repo]"),
                        root.relativize(pageFile) + " contains unresolved [repo] placeholder");
                    assertFalse(content.contains("[github-ref]"),
                        root.relativize(pageFile) + " contains unresolved [github-ref] placeholder");
                }));
    }

    private static List<Path> pagesUnder(Path root) throws IOException {
        Path variants = root.resolve("variants");
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(".html"))
                .filter(p -> !p.startsWith(variants))
                .filter(p -> !isRedirect(p))
                .sorted()
                .toList();
        }
    }

    private static String relativeUrl(Path root, Path pageFile) {
        return root.relativize(pageFile).toString().replace(java.io.File.separatorChar, '/');
    }

    // A meta refresh page has no images of its own, and navigates out from under the browser
    // while we are looking at it.
    private static boolean isRedirect(Path page) {
        try {
            return Files.readString(page).contains("http-equiv=\"refresh\"");
        } catch (IOException e) {
            return false;
        }
    }
}
