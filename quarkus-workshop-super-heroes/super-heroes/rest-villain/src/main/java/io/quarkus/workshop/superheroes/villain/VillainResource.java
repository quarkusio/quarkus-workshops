// tag::adocResource[]
package io.quarkus.workshop.superheroes.villain;

// end::adocResource[]
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;


@Path("/api/villains")
@Produces(APPLICATION_JSON)
public class VillainResource {

    private static final Logger LOGGER = Logger.getLogger(VillainResource.class);

    @Inject
    VillainService service;

    @Operation(summary = "Returns a random villain")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Villain.class, required = true)))
    // tag::adocMetrics[]
    @Counted(name = "countGetRandomVillain", description = "Counts how many times the getRandomVillain method has been invoked")
    @Timed(name = "timeGetRandomVillain", description = "Times how long it takes to invoke the getRandomVillain method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @GET
    @Path("/random")
    public Response getRandomVillain() {
        Villain villain = service.findRandomVillain();
        LOGGER.debug("Found random villain " + villain);
        return Response.ok(villain).build();
    }

    @Operation(summary = "Returns all the villains from the database")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Villain.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No villains")
    // tag::adocMetrics[]
    @Counted(name = "countGetAllVillains", description = "Counts how many times the getAllVillains method has been invoked")
    @Timed(name = "timeGetAllVillains", description = "Times how long it takes to invoke the getAllVillains method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @GET
    public Response getAllVillains() {
        List<Villain> villains = service.findAllVillains();
        LOGGER.debug("Total number of villains " + villains);
        return Response.ok(villains).build();
    }

    @Operation(summary = "Returns a villain for a given identifier")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Villain.class)))
    @APIResponse(responseCode = "204", description = "The villain is not found for a given identifier")
    // tag::adocMetrics[]
    @Counted(name = "countGetVillain", description = "Counts how many times the getVillain method has been invoked")
    @Timed(name = "timeGetVillain", description = "Times how long it takes to invoke the getVillain method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @GET
    @Path("/{id}")
    public Response getVillain(@Parameter(description = "Villain identifier", required = true) @PathParam("id") Long id) {
        Villain villain = service.findVillainById(id);
        if (villain != null) {
            LOGGER.debug("Found villain " + villain);
            return Response.ok(villain).build();
        } else {
            LOGGER.debug("No villain found with id " + id);
            return Response.noContent().build();
        }
    }

    @Operation(summary = "Creates a valid villain")
    @APIResponse(responseCode = "201", description = "The URI of the created villain", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = URI.class)))
    // tag::adocMetrics[]
    @Counted(name = "countCreateVillain", description = "Counts how many times the createVillain method has been invoked")
    @Timed(name = "timeCreateVillain", description = "Times how long it takes to invoke the createVillain method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @POST
    public Response createVillain(@RequestBody(required = true, content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Villain.class)))  @Valid Villain villain, @Context UriInfo uriInfo) {
        villain = service.persistVillain(villain);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(Long.toString(villain.id));
        LOGGER.debug("New villain created with URI " + builder.build().toString());
        return Response.created(builder.build()).build();
    }

    @Operation(summary = "Updates an exiting  villain")
    @APIResponse(responseCode = "200", description = "The updated villain", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Villain.class)))
    // tag::adocMetrics[]
    @Counted(name = "countUpdateVillain", description = "Counts how many times the updateVillain method has been invoked")
    @Timed(name = "timeUpdateVillain", description = "Times how long it takes to invoke the updateVillain method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @PUT
    public Response updateVillain(@RequestBody(required = true, content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Villain.class))) @Valid Villain villain) {
        villain = service.updateVillain(villain);
        LOGGER.debug("Villain updated with new valued " + villain);
        return Response.ok(villain).build();
    }

    @Operation(summary = "Deletes an exiting villain")
    @APIResponse(responseCode = "204")
    // tag::adocMetrics[]
    @Counted(name = "countDeleteVillain", description = "Counts how many times the deleteVillain method has been invoked")
    @Timed(name = "timeDeleteVillain", description = "Times how long it takes to invoke the deleteVillain method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @DELETE
    @Path("/{id}")
    public Response deleteVillain(@Parameter(description = "Villain identifier", required = true) @PathParam("id") Long id) {
        service.deleteVillain(id);
        LOGGER.debug("Villain deleted with " + id);
        return Response.noContent().build();
    }

    @GET
    @Produces(TEXT_PLAIN)
    @Path("/hello")
    public String hello() {
        return "hello";
    }
}
// end::adocResource[]
