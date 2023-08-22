package io.quarkus.workshop.superheroes.narration;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/narration")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
public class NarrationResource {

    @Inject
    NarrationService service;

    @POST
    public Response fight(Fight fight) throws Exception {
        String narration = service.narrate(fight);
        if (narration.startsWith("Lorem ipsum dolor sit amet"))
            return Response.status(203).entity(narration).build();
        else
            return Response.status(Response.Status.CREATED).entity(narration).build();
    }

    @GET
    @Path("/hello")
    public String hello() {
        return "Hello Narration Resource";
    }
}
