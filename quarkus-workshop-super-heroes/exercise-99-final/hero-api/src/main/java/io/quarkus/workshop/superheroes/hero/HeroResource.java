package io.quarkus.workshop.superheroes.hero;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/heroes")
@Produces(MediaType.APPLICATION_JSON)
public class HeroResource {

    @Inject
    HeroService service;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public List<Hero> getAllHeroes() {
        return service.getAllHeroes();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{name}")
    public Hero getHero(@PathParam("name") String name) {
        return service.getHero(name);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/ping")
    public String ping() {
        return "ping heroes";
    }
}
