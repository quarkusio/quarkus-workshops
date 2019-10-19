// tag::adocResource[]
package io.quarkus.workshop.superheroes.fight;

// end::adocResource[]
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
// tag::adocResource[]
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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/api/fights")
@Produces(APPLICATION_JSON)
public class FightResource {

    private static final Logger LOGGER = Logger.getLogger(FightResource.class);

    @Inject
    FightService service;

    // tag::adocFaultTolerance[]
    // tag::adocTimeout[]
    @ConfigProperty(name = "process.milliseconds", defaultValue="0")
    long tooManyMilliseconds;

    private void veryLongProcess() throws InterruptedException {
        Thread.sleep(tooManyMilliseconds);
    }

    // end::adocTimeout[]
    @Operation(summary = "Returns two random fighters")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Fighters.class, required = true)))
    // tag::adocMetrics[]
    @Counted(name = "countGetRandomFighters", description = "Counts how many times the getRandomFighters method has been invoked")
    @Timed(name = "timeGetRandomFighters", description = "Times how long it takes to invoke the getRandomFighters method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    // tag::adocTimeout[]
    @Timeout(250)
    // end::adocTimeout[]
    @GET
    @Path("/randomfighters")
    public Response getRandomFighters() throws InterruptedException {
        // tag::adocTimeout[]
        veryLongProcess();
        // end::adocTimeout[]
        Fighters fighters = service.findRandomFighters();
        LOGGER.debug("Get random fighters " + fighters);
        return Response.ok(fighters).build();
    }
    // end::adocFaultTolerance[]

    @Operation(summary = "Returns all the fights from the database")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Fight.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No fights")
    // tag::adocMetrics[]
    @Counted(name = "countGetAllFights", description = "Counts how many times the getAllFights method has been invoked")
    @Timed(name = "timeGetAllFights", description = "Times how long it takes to invoke the getAllFights method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @GET
    public Response getAllFights() {
        List<Fight> fights = service.findAllFights();
        LOGGER.debug("Total number of fights " + fights);
        return Response.ok(fights).build();
    }

    @Operation(summary = "Returns a fight for a given identifier")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Fight.class)))
    @APIResponse(responseCode = "204", description = "The fight is not found for a given identifier")
    // tag::adocMetrics[]
    @Counted(name = "countGetFight", description = "Counts how many times the getFight method has been invoked")
    @Timed(name = "timeGetFight", description = "Times how long it takes to invoke the getFight method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @GET
    @Path("/{id}")
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

    @Operation(summary = "Trigger a fight between two fighters")
    @APIResponse(responseCode = "200", description = "The result of the fight", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Fight.class)))
    // tag::adocMetrics[]
    @Counted(name = "countFight", description = "Counts how many times the createFight method has been invoked")
    @Timed(name = "timeFight", description = "Times how long it takes to invoke the createFight method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @POST
    public Fight fight(@RequestBody(description = "The two fighters fighting", required = true, content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Fighters.class))) @Valid Fighters fighters, @Context UriInfo uriInfo) {
        return service.persistFight(fighters);
    }

    @GET
    @Produces(TEXT_PLAIN)
    @Path("/hello")
    public String hello() {
        return "hello";
    }
}
// end::adocResource[]
