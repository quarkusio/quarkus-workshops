// tag::adocWebSocket[]
package io.quarkus.workshop.superheroes.statistics;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.reactive.messaging.Channel;
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
public class TopWinnerWebSocket {

    private static final Logger LOGGER = Logger.getLogger(TopWinnerWebSocket.class);
    private Jsonb jsonb;

    @Inject @Channel("winner-stats")
    Multi<Iterable<Score>> winners;

    private List<Session> sessions = new CopyOnWriteArrayList<>();
//    private Disposable subscription;

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
        winners
            .map(scores -> jsonb.toJson(scores))
            .subscribe().with(serialized -> sessions.forEach(session -> write(session, serialized)),
                failure -> System.out.println("Failed with " + failure),
                () -> System.out.println("Completed"));
    }

    @PreDestroy
    public void cleanup() throws Exception {
//        subscription.dispose();
        jsonb.close();
    }

    private void write(Session session, String serialized) {
        session.getAsyncRemote().sendText(serialized, result -> {
            if (result.getException() != null) {
                LOGGER.error("Unable to write message to web socket", result.getException());
            }
        });
    }
}
// end::adocWebSocket[]
