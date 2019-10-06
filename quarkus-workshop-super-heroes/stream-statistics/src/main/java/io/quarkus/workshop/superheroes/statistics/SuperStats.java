package io.quarkus.workshop.superheroes.statistics;

import io.reactivex.Flowable;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SuperStats {

    private Ranking topWinners = new Ranking(10);
    private TeamStats stats = new TeamStats();

    @Incoming("fights")
    @Outgoing("results")
    @Broadcast
    public Flowable<FightResult> toFightResults(Flowable<JsonObject> stream) {
        return stream.map(json -> json.mapTo(FightResult.class));
    }

    @Incoming("results")
    @Outgoing("team-stats")
    public Flowable<Double> computeTeamStats(Flowable<FightResult> results) {
        return results.map(fr -> stats.add(fr));
    }

    @Incoming("results")
    @Outgoing("winner-stats")
    public Flowable<Iterable<Score>> computeTopWinners(Flowable<FightResult> results) {
        return results
            .groupBy(FightResult::getWinnerName)
            .flatMap(group ->
                group.scan(0, (i, s) -> i + 1)
                    .skip(1)
                    .map(i -> new Score(group.getKey(), i)))
            .flatMapMaybe(score -> topWinners.onNewScore(score));
    }
}
