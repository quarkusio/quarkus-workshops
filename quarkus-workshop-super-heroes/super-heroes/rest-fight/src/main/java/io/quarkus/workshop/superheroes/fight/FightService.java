// tag::adocTransactional[]
package io.quarkus.workshop.superheroes.fight;

import io.quarkus.workshop.superheroes.fight.client.Hero;
// end::adocTransactional[]
import io.quarkus.workshop.superheroes.fight.client.HeroService;
// tag::adocTransactional[]
import io.quarkus.workshop.superheroes.fight.client.Villain;
// end::adocTransactional[]
import io.quarkus.workshop.superheroes.fight.client.VillainService;
import org.eclipse.microprofile.rest.client.inject.RestClient;
// tag::adocTransactional[]
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Random;

import static javax.transaction.Transactional.TxType.REQUIRED;
import static javax.transaction.Transactional.TxType.SUPPORTS;


@ApplicationScoped
@Transactional(SUPPORTS)
public class FightService {

    private static final Logger LOGGER = Logger.getLogger(FightService.class);

    private final Random random = new Random();

    // tag::adocRestClient[]
    @Inject
    @RestClient
    HeroService heroService;

    @Inject
    @RestClient
    VillainService villainService;
    // end::adocRestClient[]

    public List<Fight> findAllFights() {
        return Fight.listAll();
    }

    public Fight findFightById(Long id) {
        return Fight.findById(id);
    }

    @Transactional(REQUIRED)
    public Fight persistFight(Fighters fighters) {
        // Amazingly fancy logic to determine the winner...
        Fight fight;

        int heroAdjust = random.nextInt(20);
        int villainAdjust = random.nextInt(20);

        if ((fighters.hero.level + heroAdjust)
            > (fighters.villain.level + villainAdjust)) {
            fight = heroWon(fighters);
        } else if (fighters.hero.level < fighters.villain.level) {
            fight = villainWon(fighters);
        } else {
            fight = random.nextBoolean() ? heroWon(fighters) : villainWon(fighters);
        }

        fight.fightDate = Instant.now();
        fight.persist(fight);
        return fight;
    }

    private Fight heroWon(Fighters fighters) {
        LOGGER.info("Yes, Hero won :o)");
        Fight fight = new Fight();
        fight.winnerName = fighters.hero.name;
        fight.winnerPicture = fighters.hero.picture;
        fight.winnerLevel = fighters.hero.level;
        fight.loserName = fighters.villain.name;
        fight.loserPicture = fighters.villain.picture;
        fight.loserLevel = fighters.villain.level;
        fight.winnerTeam = "heroes";
        fight.loserTeam = "villains";
        return fight;
    }

    private Fight villainWon(Fighters fighters) {
        LOGGER.info("Gee, Villain won :o(");
        Fight fight = new Fight();
        fight.winnerName = fighters.villain.name;
        fight.winnerPicture = fighters.villain.picture;
        fight.winnerLevel = fighters.villain.level;
        fight.loserName = fighters.hero.name;
        fight.loserPicture = fighters.hero.picture;
        fight.loserLevel = fighters.hero.level;
        fight.winnerTeam = "villains";
        fight.loserTeam = "heroes";
        return fight;
    }

    // tag::adocRestClient[]
    Fighters findRandomFighters() {
        Hero hero = findRandomHero();
        Villain villain = findRandomVillain();
        Fighters fighters = new Fighters();
        fighters.hero = hero;
        fighters.villain = villain;
        return fighters;
    }


    Hero findRandomHero() {
        return heroService.findRandomHero();
    }


    Villain findRandomVillain() {
        return villainService.findRandomVillain();
    }
    // end::adocRestClient[]

    // tag::adocRestClient[]
    // tag::adocFallback[]


}
// end::adocTransactional[]
