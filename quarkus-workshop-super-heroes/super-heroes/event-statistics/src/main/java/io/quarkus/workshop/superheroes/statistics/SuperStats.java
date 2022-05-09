package io.quarkus.workshop.superheroes.statistics;

import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Consumer of {@link Fight} events from Kafka. There are 2 consumers for performing different aggregations. Each consumer writes out to its own in-memory channel.
 */
@ApplicationScoped
public class SuperStats {

    @Inject
    Logger logger;

    private final Ranking topWinners = new Ranking(10);
    private final TeamStats stats = new TeamStats();

    /**
     * Transforms the {@link Fight} stream into a stream of ratios. Each ratio indicates the running percentage of battles won by heroes.
     * @param results The {@link Fight} continuous stream
     * @return A continuous stream of percentages of battles won by heroes sent to the {@code team-stats} in-memory channel.
     */
    @Incoming("fights")
    @Outgoing("team-stats")
    public Multi<Double> computeTeamStats(Multi<Fight> results) {
        return results
            .onItem().transform(stats::add)
            .invoke(() -> logger.info("Fight received. Computed the team statistics"));
    }

    /**
     * Transforms the {@link Fight} stream into a running stream of top winners.
     * <p>
     *   The incoming stream is first grouped by {@link Fight#getWinnerName}. Then the number of wins for that winner is computed.
     * </p>
     * @param results The {@link Fight} continuous stream
     * @return A continuous stream of the top 10 winners and the number of wins for each winner
     */
    @Incoming("fights")
    @Outgoing("winner-stats")
    public Multi<Iterable<Score>> computeTopWinners(Multi<Fight> results) {
        return results
            .group().by(fight -> fight.winnerName)
            .onItem().transformToMultiAndMerge(group ->
                group
                    .onItem().scan(Score::new, this::incrementScore))
            .onItem().transform(topWinners::onNewScore)
            .invoke(() -> logger.info("Fight received. Computed the top winners"));
    }

    private Score incrementScore(Score score, Fight fight) {
        score.name = fight.winnerName;
        score.score = score.score + 1;
        return score;
    }

}
