package io.quarkus.workshop.superheroes.vilain;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/villains")
@Produces(MediaType.APPLICATION_JSON)
public class VillainResource {

    @Inject
    VillainService service;

    @GET
    public List<Villain> getAllVillains() {
        return service.getAllVillains();
    }

    @GET
    @Path("/{name}")
    public Villain getVillain(@PathParam("name") String name) {
        return service.getVillain(name);
    }

    @POST
    public Response create(@Valid Villain villain) {
        villain = service.createVillain(villain);
        return Response.status(Response.Status.CREATED).entity(villain).build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/ping")
    public String ping() {
        return "ping villains";
    }
}
