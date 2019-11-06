// tag::adocResource[]
package io.quarkus.workshop.superheroes.hero;

// end::adocResource[]
// tag::adocMetricsImports[]
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
// end::adocMetricsImports[]
// tag::adocOpenAPIImports[]
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
// end::adocOpenAPIImports[]
// tag::adocResource[]
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/api/heroes")
@Produces(APPLICATION_JSON)
public class HeroResource {

    private static final Logger LOGGER = Logger.getLogger(HeroResource.class);

    @Inject
    HeroService service;

    // tag::adocMetricsMethods[]
    // tag::adocOpenAPI[]
    @Operation(summary = "Returns a random hero")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Hero.class, required = true)))
    // end::adocOpenAPI[]
    // tag::adocMetrics[]
    @Counted(name = "countGetRandomHero", description = "Counts how many times the getRandomHero method has been invoked")
    @Timed(name = "timeGetRandomHero", description = "Times how long it takes to invoke the getRandomHero method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @GET
    @Path("/random")
    public Response getRandomHero() {
        Hero hero = service.findRandomHero();
        LOGGER.debug("Found random hero " + hero);
        return Response.ok(hero).build();
    }
    // end::adocMetricsMethods[]

    // tag::adocOpenAPI[]
    @Operation(summary = "Returns all the heroes from the database")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Hero.class, type = SchemaType.ARRAY)))
    @APIResponse(responseCode = "204", description = "No heroes")
    // end::adocOpenAPI[]
    // tag::adocMetrics[]
    @Counted(name = "countGetAllHeroes", description = "Counts how many times the getAllHeroes method has been invoked")
    @Timed(name = "timeGetAllHeroes", description = "Times how long it takes to invoke the getAllHeroes method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @GET
    public Response getAllHeroes() {
        List<Hero> heroes = service.findAllHeroes();
        LOGGER.debug("Total number of heroes " + heroes);
        return Response.ok(heroes).build();
    }

    // tag::adocOpenAPI[]
    @Operation(summary = "Returns a hero for a given identifier")
    @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Hero.class)))
    @APIResponse(responseCode = "204", description = "The hero is not found for a given identifier")
    // end::adocOpenAPI[]
    // tag::adocMetrics[]
    @Counted(name = "countGetHero", description = "Counts how many times the getHero method has been invoked")
    @Timed(name = "timeGetHero", description = "Times how long it takes to invoke the getHero method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @GET
    @Path("/{id}")
    public Response getHero(
        // tag::adocOpenAPI[]
        @Parameter(description = "Hero identifier", required = true)
        // end::adocOpenAPI[]
        @PathParam("id") Long id) {
        Hero hero = service.findHeroById(id);
        if (hero != null) {
            LOGGER.debug("Found hero " + hero);
            return Response.ok(hero).build();
        } else {
            LOGGER.debug("No hero found with id " + id);
            return Response.noContent().build();
        }
    }

    // tag::adocOpenAPI[]
    @Operation(summary = "Creates a valid hero")
    @APIResponse(responseCode = "201", description = "The URI of the created hero", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = URI.class)))
    // end::adocOpenAPI[]
    // tag::adocMetrics[]
    @Counted(name = "countCreateHero", description = "Counts how many times the createHero method has been invoked")
    @Timed(name = "timeCreateHero", description = "Times how long it takes to invoke the createHero method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @POST
    public Response createHero(
        // tag::adocOpenAPI[]
        @RequestBody(required = true, content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Hero.class)))
        // end::adocOpenAPI[]
        @Valid Hero hero, @Context UriInfo uriInfo) {
        hero = service.persistHero(hero);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(Long.toString(hero.id));
        LOGGER.debug("New hero created with URI " + builder.build().toString());
        return Response.created(builder.build()).build();
    }

    // tag::adocOpenAPI[]
    @Operation(summary = "Updates an exiting  hero")
    @APIResponse(responseCode = "200", description = "The updated hero", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Hero.class)))
    // end::adocOpenAPI[]
    // tag::adocMetrics[]
    @Counted(name = "countUpdateHero", description = "Counts how many times the updateHero method has been invoked")
    @Timed(name = "timeUpdateHero", description = "Times how long it takes to invoke the updateHero method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @PUT
    public Response updateHero(
        // tag::adocOpenAPI[]
        @RequestBody(required = true, content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Hero.class)))
        // end::adocOpenAPI[]
        @Valid Hero hero) {
        hero = service.updateHero(hero);
        LOGGER.debug("Hero updated with new valued " + hero);
        return Response.ok(hero).build();
    }

    // tag::adocOpenAPI[]
    @Operation(summary = "Deletes an exiting hero")
    @APIResponse(responseCode = "204")
    // end::adocOpenAPI[]
    // tag::adocMetrics[]
    @Counted(name = "countDeleteHero", description = "Counts how many times the deleteHero method has been invoked")
    @Timed(name = "timeDeleteHero", description = "Times how long it takes to invoke the deleteHero method", unit = MetricUnits.MILLISECONDS)
    // end::adocMetrics[]
    @DELETE
    @Path("/{id}")
    public Response deleteHero(
        // tag::adocOpenAPI[]
        @Parameter(description = "Hero identifier", required = true)
        // end::adocOpenAPI[]
        @PathParam("id") Long id) {
        service.deleteHero(id);
        LOGGER.debug("Hero deleted with " + id);
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
