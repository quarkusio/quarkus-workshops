package io.quarkus.workshop.superheroes.narration;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

@Path("/api/narration")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "narration")
public class NarrationResource {

    @Inject
    Logger logger;

    @Inject
    NarrationService service;

    @POST
    @Timeout(30_000)
    @Fallback(fallbackMethod = "fallbackNarrate")
    public Response narrate(Fight fight) {
        String narration = service.narrate(fight);
        return Response.status(Response.Status.CREATED).entity(narration).build();
    }

    public Response fallbackNarrate(Fight fight) {
        logger.warn("Falling back on Narration");
        String fallback = """
            High above a bustling city, a symbol of hope and justice soared through the sky, \
            while chaos reigned below, with malevolent laughter echoing through the streets.
            With unwavering determination, the figure swiftly descended, effortlessly evading \
            explosive attacks, closing the gap, and delivering a decisive blow that silenced \
            the wicked laughter.

            In the end, the battle concluded with a clear victory for the forces of good, \
            as their commitment to peace triumphed over the chaos and villainy that had \
            threatened the city.
            The people knew that their protector had once again ensured their safety.
            """;
        return Response.status(Response.Status.CREATED).entity(fallback).build();
    }

    @GET
    @Path("/hello")
    public String hello() {
        return "Hello Narration Resource";
    }
}
