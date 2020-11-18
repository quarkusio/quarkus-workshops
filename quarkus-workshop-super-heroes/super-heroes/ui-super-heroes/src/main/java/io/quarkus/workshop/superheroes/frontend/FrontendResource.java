package io.quarkus.workshop.superheroes.frontend;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/api/frontend")
public class FrontendResource {
    @ConfigProperty(name = "base.path")
    String basePath;

    @GET
    @Path("basePath")
    @Produces(MediaType.TEXT_PLAIN)
    public String basePath() {
        return basePath;
    }
}