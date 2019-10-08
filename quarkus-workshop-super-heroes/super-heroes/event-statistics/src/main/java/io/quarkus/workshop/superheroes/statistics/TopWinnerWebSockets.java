package io.quarkus.workshop.superheroes.statistics;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.smallrye.reactive.messaging.annotations.Stream;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ServerEndpoint("/stats/winners")
@ApplicationScoped
public class TopWinnerWebSockets {

    private static final Logger LOGGER = Logger.getLogger(TopWinnerWebSockets.class);
    private Jsonb jsonb;

    @Inject @Stream("winner-stats") Flowable<Iterable<Score>> winners;

    private List<Session> sessions = new CopyOnWriteArrayList<>();
    private Disposable subscription;

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
        jsonb = JsonbBuilder.create();
        subscription = winners.subscribe(scores -> sessions.forEach(session -> write(session, scores)));
    }

    @PreDestroy
    public void cleanup() throws Exception {
        subscription.dispose();
        jsonb.close();
    }

    private void write(Session session, Iterable<Score> scores) {
        session.getAsyncRemote().sendText(jsonb.toJson(scores), result -> {
            if (result.getException() != null) {
                LOGGER.error("Unable to write message to web socket", result.getException());
            }
        });
    }
}
