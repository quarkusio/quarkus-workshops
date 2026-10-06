package io.quarkus.workshop.docs;

import java.nio.file.Path;
import java.util.Map;

import io.mvnpm.raclette.types.Status;
import io.mvnpm.raclette.types.Uri;
import io.quarkiverse.roq.testing.RoqAndRoll;
import io.quarkiverse.roq.testing.RoqLinks;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that every link the site makes to itself goes somewhere.
 * <p>
 * This complements {@link DeadImagesTest}, which drives a browser over each page in turn and so
 * only sees {@code <img>}. Roq's own link checker walks the whole generated tree at once and
 * covers {@code <a href>} too, including the links between chapters that no single page test
 * would catch.
 * <p>
 * Only internal links are checked. {@code RoqLinks.checkAll()} would also follow every external
 * url, which makes the build depend on the network being up and on other people's sites staying
 * put — a useful thing to run, but not on every commit.
 */
@QuarkusTest
@RoqAndRoll(port = RoqSiteTest.ROQ_PORT)
public class DeadLinksTest {

    @Test
    @DisplayName("Every internal link in the generated site should resolve")
    void siteShouldHaveNoBrokenInternalLinks() {
        Path root = RoqLinks.outputDir();
        Map<Uri, Status> broken = RoqLinks.checkInternal();

        StringBuilder message = new StringBuilder(broken.size() + " broken internal link(s):");
        broken.forEach((uri, status) -> message.append("\n  ")
            .append(relativise(root, uri.toString()))
            .append(" -> ")
            .append(status));

        assertTrue(broken.isEmpty(), message.toString());
    }

    // The checker reports absolute file: urls, which bury the interesting part of the path in a
    // line of build directory.
    private static String relativise(Path root, String uri) {
        String prefix = root.toUri().toString();
        return uri.startsWith(prefix) ? "/" + uri.substring(prefix.length()):uri;
    }
}
