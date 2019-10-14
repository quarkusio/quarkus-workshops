// tag::adocRestClient[]
package io.quarkus.workshop.superheroes.fight.client;

// end::adocRestClient[]
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

// tag::adocRestClient[]
@Path("/api/heroes")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient
public interface HeroService {

    @GET
    @Path("/random")
    Hero findRandomHero();
}
// end::adocRestClient[]
