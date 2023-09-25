package io.quarkus.workshop.superheroes.fight.client;

import io.quarkus.workshop.superheroes.fight.Fight;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/narration")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "narration")
public interface NarrationProxy {

    @POST
    String narrate(Fight fight);
}
