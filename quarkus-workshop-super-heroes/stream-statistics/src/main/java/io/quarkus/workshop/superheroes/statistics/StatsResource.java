package io.quarkus.workshop.superheroes.statistics;

import io.reactivex.Flowable;
import io.smallrye.reactive.messaging.annotations.Stream;
import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/stats")
public class StatsResource {

    @Inject @Stream("winner-stats") Flowable<Score> winners;

    @GET
    @Path("/winners")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType("application/json")
    public Publisher<Score> top() {
        return winners;
    }
}
