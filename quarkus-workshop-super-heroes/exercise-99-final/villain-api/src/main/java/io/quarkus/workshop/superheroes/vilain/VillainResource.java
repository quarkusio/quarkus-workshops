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
    @Produces(MediaType.TEXT_PLAIN)
    public List<Villain> getAllVilains() {
        return service.getAllVilains();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{name}")
    public Villain getVilain(@PathParam("name") String name) {
        return service.getVilain(name);
    }


    @Path("/ping")
    public String ping() {
        return "ping villains";
    }
}
