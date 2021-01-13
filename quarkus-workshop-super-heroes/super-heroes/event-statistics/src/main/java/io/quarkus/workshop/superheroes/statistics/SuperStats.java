// tag::adocBean[]
package io.quarkus.workshop.superheroes.statistics;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SuperStats {

    private final Ranking topWinners = new Ranking(10);
    private final TeamStats stats = new TeamStats();

    @Incoming("fights")
    @Outgoing("team-stats")
    public Multi<Double> computeTeamStats(Multi<Fight> results) {
        return results
            .onItem().transform(stats::add);
    }

    @Incoming("fights")
    @Outgoing("winner-stats")
    public Multi<Iterable<Score>> computeTopWinners(Multi<Fight> results) {
        return results
            .groupItems().by(fight -> fight.winnerName)
            .onItem().transformToMultiAndMerge(group ->
                group
                    .onItem().scan(Score::new, this::incrementScore))
            .onItem().transform(topWinners::onNewScore);
    }

    private Score incrementScore(Score score, Fight fight) {
        score.name = fight.winnerName;
        score.score = score.score + 1;
        return score;

    }

}
// end::adocBean[]
