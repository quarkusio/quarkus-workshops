package io.quarkus.workshop.superheroes.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.microprofile.reactive.messaging.Channel;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// tag::adocJavadoc[]
/**
 * WebSocket endpoint for the {@code /stats/winners} endpoint. Exposes the {@code winner-stats} channel over the socket to anyone listening.
 * <p>
 *   Uses constructor injection over field injection to show how it is done.
 * </p>
 */
// end::adocJavadoc[]
@ServerEndpoint("/stats/winners")
@ApplicationScoped
public class TopWinnerWebSocket {

    @Inject ObjectMapper mapper;

    @Channel("winner-stats")
    Multi<Iterable<Score>> winners;

    private final List<Session> sessions = new CopyOnWriteArrayList<>();
    private Cancellable cancellable;

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @PostConstruct
    public void subscribe() {
        cancellable = winners
            .map(Unchecked.function(scores -> mapper.writeValueAsString(scores)))
            .subscribe().with(serialized -> sessions.forEach(session -> write(session, serialized)));
    }

    @PreDestroy
    public void cleanup() {
        cancellable.cancel();
    }

    private void write(Session session, String serialized) {
        session.getAsyncRemote().sendText(serialized, result -> {
            if (result.getException() != null) {
                Log.error("Unable to write message to web socket", result.getException());
            }
        });
    }
}
