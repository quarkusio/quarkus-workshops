// tag::adocBean[]
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
            // Create a sub-stream per hero and villain - for each winner, we get a new sub-stream
            .groupBy(FightResult::getWinnerName)
            // For each of these sub-stream
            .flatMap(group ->
                // Compute the new score (increment the score by one)
                group.scan(0, (i, s) -> i + 1)
                    // Skip the initial 0, oddity of the scan operator
                    .skip(1)
                    // Creates the Score object
                    .map(i -> new Score(group.getKey(), i)))
            // For every Score emitted by the sub-streams, add it to the
            // ranking object and check if it changes the top 10.
            .flatMapMaybe(score -> topWinners.onNewScore(score));
    }
}
// end::adocBean[]
