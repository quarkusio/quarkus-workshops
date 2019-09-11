package io.quarkus.workshop.superheroes.vilain;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/ping")
    public String ping() {
        return "ping villains";
    }
}
