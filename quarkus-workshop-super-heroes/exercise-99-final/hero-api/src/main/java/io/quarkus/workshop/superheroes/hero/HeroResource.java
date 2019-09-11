package io.quarkus.workshop.superheroes.hero;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/hero")
public class HeroResource {


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Hero> getAll() {
        return Hero.listAll();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Hero getOne(@PathParam("id") long id) {
        return Hero.findById(id);
    }
}
