package io.quarkus.workshop.superheroes.hero;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/heroes")
@Produces(MediaType.APPLICATION_JSON)
public class HeroResource {

    @Inject
    HeroService service;

    @GET
    public List<Hero> getAllHeroes() {
        return service.getAllHeroes();
    }

    @GET
    @Path("/{name}")
    public Hero getHero(@PathParam("name") String name) {
        return service.getHero(name);
    }

    @POST
    public Response create(@Valid Hero hero) {
        hero = service.createHero(hero);
        return Response.status(Response.Status.CREATED).entity(hero).build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/ping")
    public String ping() {
        return "ping heroes";
    }
}
