// tag::adocBean[]
package io.quarkus.workshop.superheroes.statistics;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.annotations.Broadcast;
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
    public Multi<FightResult> toFightResults(Multi<Fight> fights) {
         return fights
            .onItem().scan(
                FightResult::new,
                this::compute
            );
    }

    private FightResult compute(FightResult s, Fight f) {
        s.setFightDate(f.fightDate);
        s.setWinnerName(f.winnerName);
        s.setLoserName(f.loserName);
        return s;
    }


    @Incoming("results")
    @Outgoing("team-stats")
    public Multi<Double> computeTeamStats(Multi<FightResult> results) {
        return results.map(fr -> stats.add(fr));
    }

    @Incoming("results")
    @Outgoing("winner-stats")
    public Multi<Iterable<Score>> computeTopWinners(Multi<FightResult> results) {
        // Create a sub-stream per hero and villain - for each winner, we get a new sub-stream
        Multi<Uni<Iterable<Score>>> uniMulti = results.groupItems().by(FightResult::getWinnerName)
            .flatMap(group -> group.onItem()
                // For each of these sub-stream
                //Compute the new score (increment the score by one) and creates the Score object
                .scan(Score::new, this::incrementScore)
                // For every Score emitted by the sub-streams, add it to the
                // ranking object and check if it changes the top 10.
                .map(score -> topWinners.onNewScore(score)));

        return uniMulti.flatMap(iterableUni -> iterableUni.toMulti());

    }

    private Score incrementScore(Score score,FightResult fightResult){
        score.name =  fightResult.getWinnerName();
        score.score = score.score +1;
        return score;

    }

}
// end::adocBean[]
