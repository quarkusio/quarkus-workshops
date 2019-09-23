package io.quarkus.workshop.superheroes.fight;

import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/api/fights")
@Produces(MediaType.APPLICATION_JSON)
public class FightResource {

    private static final Logger LOGGER = Logger.getLogger(FightResource.class);

    @Inject
    FightService service;

    @GET
    @Path("/randomfighters")
    public Response getRandomFighters() {
        Fighters fighters = service.getRandomFighters();
        LOGGER.debug("Get random fighters " + fighters);
        return Response.ok(fighters).build();
    }

    @GET
    public Response getAllFights() {
        List<Fight> fights = service.getAllFights();
        LOGGER.debug("Total number of fights " + fights);
        return Response.ok(fights).build();
    }

    @GET
    @Path("/{id}")
    public Response getFight(@PathParam("id") Long id) {
        Fight fight = service.findFightById(id);
        if (fight != null) {
            LOGGER.debug("Found fight " + fight);
            return Response.ok(fight).build();
        } else {
            LOGGER.debug("No fight found with id " + id);
            return Response.noContent().build();
        }
    }

    @POST
    public Response create(Fighters fighters, @Context UriInfo uriInfo) {
        Fight fight = service.createFight(fighters);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(Long.toString(fight.id));
        LOGGER.debug("New fight created with URI " + builder.build().toString());
        return Response.created(builder.build()).build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/ping")
    public Response ping() {
        LOGGER.debug("Invoking Ping");
        return Response.ok("ping fights").build();
    }
}
