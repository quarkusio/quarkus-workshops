package io.quarkus.tools;

import io.quarkus.arc.profile.IfBuildProfile;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Serves the diagrams asciidoctor-diagram renders in dev mode, which go to target/ rather than
 * public/ (see imagesoutdir in config/application.properties). Requests for any other image fall
 * through to Roq, which serves public/images as usual.
 */
@IfBuildProfile("dev")
@ApplicationScoped
public class DevDiagramRoute {

    @ConfigProperty(name = "quarkus.asciidoc.attributes.imagesoutdir")
    String imagesOutDir;

    @ConfigProperty(name = "quarkus.asciidoc.attributes.imagesdir")
    String imagesDir;

    void register(@Observes Router router) {
        // Ahead of Roq's routes. A diagram is re-rendered whenever its page is, so nothing is cached.
        router.route(imagesDir + "/*")
                .order(-1)
                .handler(StaticHandler.create(FileSystemAccess.ROOT, imagesOutDir)
                        .setCachingEnabled(false));
    }
}
