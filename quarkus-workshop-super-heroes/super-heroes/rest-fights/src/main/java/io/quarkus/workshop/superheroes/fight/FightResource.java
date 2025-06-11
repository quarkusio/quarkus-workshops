package io.quarkus.workshop.superheroes.fight;

import io.quarkus.logging.Log;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
// tag::adocFaultTolerance[]
import org.eclipse.microprofile.faulttolerance.Timeout;
// end::adocFaultTolerance[]

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

// tag::adocJavadoc[]

/**
 * JAX-RS API endpoints with {@code /api/fights} as the base URI for all endpoints
 */
// end::adocJavadoc[]
@Path("/api/fights")
@Produces(APPLICATION_JSON)
@ApplicationScoped
public class FightResource {

    @Inject
    FightService service;

    // tag::adocFaultTolerance[]
    @ConfigProperty(name = "process.milliseconds", defaultValue = "0")
    long tooManyMilliseconds;

    private void veryLongProcess() {
        try {
            Thread.sleep(tooManyMilliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    // end::adocFaultTolerance[]

    @GET
    @Path("/randomfighters")
    // tag::adocFaultTolerance[]
    @Timeout(500) // <-- Added
    // end::adocFaultTolerance[]
    public Response getRandomFighters() {
        // tag::adocFaultTolerance[]
        //  veryLongProcess(); // <-- Added
        // end::adocFaultTolerance[]
        Fighters fighters = service.findRandomFighters();
        Log.debug("Get random fighters " + fighters);
        return Response.ok(fighters).build();
    }

    @GET
    public Response getAllFights() {
        List<Fight> fights = service.findAllFights();
        Log.debug("Total number of fights " + fights);
        return Response.ok(fights).build();
    }

    @GET
    @Path("/{id}")
    public Response getFight(Long id) {
        Fight fight = service.findFightById(id);
        if (fight != null) {
            Log.debug("Found fight " + fight);
            return Response.ok(fight).build();
        } else {
            Log.debug("No fight found with id " + id);
            return Response.noContent().build();
        }
    }

    @POST
    public Fight fight(@Valid Fighters fighters, UriInfo uriInfo) {
        return service.persistFight(fighters);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello")
    public String hello() {
        return "Hello Fight Resource";
    }

    // tag::adocNarrate[]
    @POST
    @Path("/narrate")
    @Consumes(APPLICATION_JSON)
    @Produces(TEXT_PLAIN)
    public Response narrateFight(@Valid Fight fight) {
        Log.debug("Narrate the fight " + fight);
        String narration = service.narrateFight(fight);
        return Response.status(Response.Status.CREATED).entity(narration).build();

    }
    // end::adocNarrate[]
}
