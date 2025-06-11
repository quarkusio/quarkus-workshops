package io.quarkus.workshop.superheroes.fight;


import io.quarkus.logging.Log;
import io.quarkus.workshop.superheroes.fight.client.*;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Random;

import static jakarta.transaction.Transactional.TxType.REQUIRED;
import static jakarta.transaction.Transactional.TxType.SUPPORTS;

/**
 * Business logic for the Fight service
 */
@ApplicationScoped
@Transactional(SUPPORTS)
public class FightService {

    @RestClient
    HeroProxy heroProxy;
    @RestClient
    VillainProxy villainProxy;

    // tag::adocNarrate[]
    @RestClient
    NarrationProxy narrationProxy;
    // end::adocNarrate[]

    private final Random random = new Random();

    @Channel("fights")
    Emitter<Fight> emitter;

    public List<Fight> findAllFights() {
        return Fight.listAll();
    }

    public Fight findFightById(Long id) {
        return Fight.findById(id);
    }

    Fighters findRandomFighters() {
        Hero hero = findRandomHero();
        Villain villain = findRandomVillain();
        Fighters fighters = new Fighters();
        fighters.hero = hero;
        fighters.villain = villain;
        return fighters;
    }

    // tag::adocFallback[]
    @Fallback(fallbackMethod = "fallbackRandomHero")
    Hero findRandomHero() {
        return heroProxy.findRandomHero();
    }

    @Fallback(fallbackMethod = "fallbackRandomVillain")
    Villain findRandomVillain() {
        return villainProxy.findRandomVillain();
    }

    public Hero fallbackRandomHero() {
        Log.warn("Falling back on Hero");
        Hero hero = new Hero();
        hero.name = "Fallback hero";
        hero.picture = "https://dummyimage.com/240x320/1e8fff/ffffff&text=Fallback+Hero";
        hero.powers = "Fallback hero powers";
        hero.level = 1;
        return hero;
    }

    public Villain fallbackRandomVillain() {
        Log.warn("Falling back on Villain");
        Villain villain = new Villain();
        villain.name = "Fallback villain";
        villain.picture = "https://dummyimage.com/240x320/b22222/ffffff&text=Fallback+Villain";
        villain.powers = "Fallback villain powers";
        villain.level = 42;
        return villain;
    }
    // end::adocFallback[]

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
        fight.persist();

        Log.info("Fight sent to statistics");
        emitter.send(fight).toCompletableFuture().join();

        return fight;
    }

    private Fight heroWon(Fighters fighters) {
        Log.info("Yes, Hero won :o)");
        Fight fight = new Fight();
        fight.winnerName = fighters.hero.name;
        fight.winnerPicture = fighters.hero.picture;
        fight.winnerLevel = fighters.hero.level;
        fight.winnerPowers = fighters.hero.powers;
        fight.loserName = fighters.villain.name;
        fight.loserPicture = fighters.villain.picture;
        fight.loserLevel = fighters.villain.level;
        fight.loserPowers = fighters.villain.powers;
        fight.winnerTeam = "heroes";
        fight.loserTeam = "villains";
        return fight;
    }

    private Fight villainWon(Fighters fighters) {
        Log.info("Gee, Villain won :o(");
        Fight fight = new Fight();
        fight.winnerName = fighters.villain.name;
        fight.winnerPicture = fighters.villain.picture;
        fight.winnerLevel = fighters.villain.level;
        fight.winnerPowers = fighters.villain.powers;
        fight.loserName = fighters.hero.name;
        fight.loserPicture = fighters.hero.picture;
        fight.loserLevel = fighters.hero.level;
        fight.loserPowers = fighters.hero.powers;
        fight.winnerTeam = "villains";
        fight.loserTeam = "heroes";
        return fight;
    }

    // tag::adocNarrate[]

    public String narrateFight(Fight fight) {
        return narrationProxy.narrate(fight);
    }
    // end::adocNarrate[]
}
