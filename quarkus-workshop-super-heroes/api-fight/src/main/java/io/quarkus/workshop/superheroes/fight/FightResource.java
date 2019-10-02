package io.quarkus.workshop.superheroes.fight;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/fights")
@Produces(APPLICATION_JSON)
public class FightResource {

    private static final Logger LOGGER = Logger.getLogger(FightResource.class);

    @Inject
    FightService service;

    @GET
    @Path("/randomfighters")
    @Operation(summary = "Returns two random fighters")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Fighters.class, required = true)))
    @Counted(name = "countGetRandomFighters", description = "Counts how many times the getRandomFighters method has been invoked")
    @Timed(name = "timeGetRandomFighters", description = "Times how long it takes to invoke the getRandomFighters method", unit = MetricUnits.MILLISECONDS)
    public Response getRandomFighters() {
        Fighters fighters = service.findRandomFighters();
        LOGGER.debug("Get random fighters " + fighters);
        return Response.ok(fighters).build();
    }

    @GET
    @Operation(summary = "Returns all the fights from the database")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Fight.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No fights")
    @Counted(name = "countGetAllFights", description = "Counts how many times the getAllFights method has been invoked")
    @Timed(name = "timeGetAllFights", description = "Times how long it takes to invoke the getAllFights method", unit = MetricUnits.MILLISECONDS)
    public Response getAllFights() {
        List<Fight> fights = service.findAllFights();
        LOGGER.debug("Total number of fights " + fights);
        return Response.ok(fights).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Returns a fight for a given identifier")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Fight.class)))
    @APIResponse(responseCode = "204", description = "The fight is not found for a given identifier")
    @Counted(name = "countGetFight", description = "Counts how many times the getFight method has been invoked")
    @Timed(name = "timeGetFight", description = "Times how long it takes to invoke the getFight method", unit = MetricUnits.MILLISECONDS)
    public Response getFight(@Parameter(description = "Fight identifier", required = true) @PathParam("id") Long id) {
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
    @Operation(summary = "Creates a fight between two fighters")
    @APIResponse(responseCode = "201", description = "The URI of the created fight", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = URI.class)))
    @Counted(name = "countCreateFight", description = "Counts how many times the createFight method has been invoked")
    @Timed(name = "timeCreateFight", description = "Times how long it takes to invoke the createFight method", unit = MetricUnits.MILLISECONDS)
    public Response createFight(@RequestBody(description = "The two fighters fighting", required = true, content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Fighters.class))) @Valid Fighters fighters, @Context UriInfo uriInfo) {
        Fight fight = service.persistFight(fighters);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(Long.toString(fight.id));
        LOGGER.debug("New fight created with URI " + builder.build().toString());
        return Response.created(builder.build()).build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello")
    public String hello() {
        return "hello";
    }
}
