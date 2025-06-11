package io.quarkus.workshop.superheroes.statistics;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import jakarta.enterprise.context.ApplicationScoped;

// tag::adocJavadoc[]

/**
 * Consumer of {@link Fight} events from Kafka. There are 2 consumers for performing different aggregations. Each consumer writes out to its own in-memory channel.
 */
// end::adocJavadoc[]
@ApplicationScoped
public class SuperStats {

    private final Ranking topWinners = new Ranking(10);
    private final TeamStats stats = new TeamStats();

    // tag::adocJavadoc[]
    /**
     * Transforms the {@link Fight} stream into a stream of ratios. Each ratio indicates the running percentage of battles won by heroes.
     *
     * @param results The {@link Fight} continuous stream
     * @return A continuous stream of percentages of battles won by heroes sent to the {@code team-stats} in-memory channel.
     */
    // end::adocJavadoc[]
    @Incoming("fights")
    @Outgoing("team-stats")
    public Multi<Double> computeTeamStats(Multi<Fight> results) {
        return results
            .onItem().transform(stats::add)
            .invoke(() -> Log.info("Fight received. Computed the team statistics"));
    }

    // tag::adocJavadoc[]
    /**
     * Transforms the {@link Fight} stream into a running stream of top winners.
     * <p>
     * The incoming stream is first grouped by {@link Fight#winnerName}. Then the number of wins for that winner is computed.
     * </p>
     *
     * @param results The {@link Fight} continuous stream
     * @return A continuous stream of the top 10 winners and the number of wins for each winner
     */
    // end::adocJavadoc[]
    @Incoming("fights")
    @Outgoing("winner-stats")
    public Multi<Iterable<Score>> computeTopWinners(Multi<Fight> results) {
        return results
            .group().by(fight -> fight.winnerName)
            .onItem().transformToMultiAndMerge(group ->
                group
                    .onItem().scan(Score::new, this::incrementScore))
            .onItem().transform(topWinners::onNewScore)
            .invoke(() -> Log.info("Fight received. Computed the top winners"));
    }

    private Score incrementScore(Score score, Fight fight) {
        score.name = fight.winnerName;
        score.score = score.score + 1;
        return score;
    }
}
