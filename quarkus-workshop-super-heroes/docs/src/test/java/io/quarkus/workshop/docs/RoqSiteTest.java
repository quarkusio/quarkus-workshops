package io.quarkus.workshop.docs;

import java.nio.file.Path;

import io.quarkiverse.roq.testing.RoqLinks;

/**
 * Base for the tests that run in the {@code test} phase against a site Roq generates in-process.
 * <p>
 * Subclasses carry {@code @QuarkusTest} and {@code @RoqAndRoll}: between them those generate the
 * static site and serve it on {@link #ROQ_PORT}, so the tests need no prior {@code mvn package}
 * and run from an IDE unchanged. The cost is that the site is regenerated once per annotated
 * class, which is why there are only a handful of them.
 * <p>
 * What this generates is the canonical site — every page under {@code content/}. It does not
 * include {@code variants/}, which is assembled later in the build by a Roq run per variant;
 * anything that needs those belongs in an {@code *IT} on top of {@link BrowserIT} instead.
 */
public abstract class RoqSiteTest extends BrowserTestBase {

    /** The default {@code @RoqAndRoll} port; repeated here because the annotation needs a constant. */
    static final int ROQ_PORT = 8082;

    @Override
    protected String baseUrl() {
        return "http://localhost:" + ROQ_PORT;
    }

    /**
     * Where the site was generated. Only valid once the test instance exists, since
     * {@code @RoqAndRoll} generates the site after construction — so this must not be called from
     * a {@code @MethodSource} factory or a static initialiser.
     */
    protected static Path outputDir() {
        return RoqLinks.outputDir();
    }
}
