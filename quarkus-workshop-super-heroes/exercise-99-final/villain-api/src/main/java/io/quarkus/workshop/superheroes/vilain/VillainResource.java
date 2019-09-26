package io.quarkus.workshop.superheroes.vilain;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
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

@Path("/api/villains")
@Produces(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(info = @Info(title = "Villain API", description = "This API allows CRUD operations on a villain", version = "1.0"))
public class VillainResource {

    private static final Logger LOGGER = Logger.getLogger(VillainResource.class);

    @Inject
    VillainService service;

    @GET
    @Path("/random")
    @Operation(summary = "Returns a random villain")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Villain.class, required = true)))
    public Response getRandomVillain() {
        Villain villain = service.findRandomVillain();
        LOGGER.debug("Found random villain " + villain);
        return Response.ok(villain).build();
    }

    @GET
    @Operation(summary = "Returns all the villains from the database")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Villain.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No villains")
    public Response getAllVillains() {
        List<Villain> villains = service.getAllVillains();
        LOGGER.debug("Total number of villains " + villains);
        return Response.ok(villains).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Returns a villain for a given identifier")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Villain.class)))
    @APIResponse(responseCode = "204", description = "The villain is not found for a given identifier")
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

    @POST
    @Operation(summary = "Creates a valid villain")
    @APIResponse(responseCode = "201", description = "The URI of the created villain", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = URI.class)))
    public Response create(@RequestBody(required = true, content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Villain.class)))  @Valid Villain villain, @Context UriInfo uriInfo) {
        villain = service.createVillain(villain);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(Long.toString(villain.id));
        LOGGER.debug("New villain created with URI " + builder.build().toString());
        return Response.created(builder.build()).build();
    }

    @PUT
    @Operation(summary = "Updates an exiting  villain")
    @APIResponse(responseCode = "200", description = "The updated villain", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Villain.class)))
    public Response update(@RequestBody(required = true, content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Villain.class))) @Valid Villain villain) {
        villain = service.updateVillain(villain);
        LOGGER.debug("Villain updated with new valued " + villain);
        return Response.ok(villain).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Deletes an exiting villain")
    @APIResponse(responseCode = "204", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Villain.class)))
    public Response delete(@Parameter(description = "Villain identifier", required = true) @PathParam("id") Long id) {
        service.deleteVillain(id);
        LOGGER.debug("Villain deleted with " + id);
        return Response.noContent().build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/ping")
    @Operation(summary = "Pings the Villain REST Endpoint")
    public Response ping() {
        LOGGER.debug("Invoking Ping");
        return Response.ok("ping villains").build();
    }
}
