package io.quarkus.workshop.superheroes.statistics;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.logging.Logger;

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
 * WebSocket endpoint for the {@code /stats/team} endpoint. Exposes the {@code team-stats} channel over the socket to anyone listening.
 * <p>
 *   Uses field injection via {@link Inject @Inject} over construction injection to show how it is done
 * </p>
 */
// end::adocJavadoc[]
@ServerEndpoint("/stats/team")
@ApplicationScoped
public class TeamStatsWebSocket {

    @Channel("team-stats")
    Multi<Double> stream;

    @Inject Logger logger;

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
        cancellable = stream.subscribe()
            .with(ratio -> sessions.forEach(session -> write(session, ratio)));
    }

    @PreDestroy
    public void cleanup() {
        cancellable.cancel();
    }

    private void write(Session session, double ratio) {
        session.getAsyncRemote().sendText(Double.toString(ratio), result -> {
            if (result.getException() != null) {
                logger.error("Unable to write message to web socket", result.getException());
            }
        });
    }
}
