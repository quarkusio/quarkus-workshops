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
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ServerEndpoint("/stats/team")
@ApplicationScoped
public class TeamStatsWebSocket {

    @Inject
    @Channel("team-stats")
    Multi<Double> stream;

    private static final Logger LOGGER = Logger.getLogger(TeamStatsWebSocket.class);

    private List<Session> sessions = new CopyOnWriteArrayList<>();

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
        stream.subscribe().with(ratio -> sessions.forEach(session -> write(session, ratio)),
            failure -> System.out.println("Failed with " + failure),
            () -> System.out.println("Completed"));
    }

//    @PreDestroy
//    public void cleanup() {
//        subscription.dispose();
//    }

    private void write(Session session, double ratio) {
        session.getAsyncRemote().sendText(Double.toString(ratio), result -> {
            if (result.getException() != null) {
                LOGGER.error("Unable to write message to web socket", result.getException());
            }
        });
    }
}
// end::adocWebSocket[]
