// tag::adocBean[]
package io.quarkus.workshop.superheroes.statistics;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;

@RegisterForReflection
public class FightResult {

    private long id;
    private Instant fightDate;
    private String winnerName;
    private int winnerLevel;
    private String winnerPicture;
    private String loserName;
    private int loserLevel;
    private String loserPicture;
    private String winnerTeam;
    private String loserTeam;

    // Getters and Setters
    // tag::adocSkip[]
    public long getId() {
        return id;
    }

    public FightResult setId(long id) {
        this.id = id;
        return this;
    }

    public Instant getFightDate() {
        return fightDate;
    }

    public FightResult setFightDate(Instant fightDate) {
        this.fightDate = fightDate;
        return this;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public FightResult setWinnerName(String winnerName) {
        this.winnerName = winnerName;
        return this;
    }

    public int getWinnerLevel() {
        return winnerLevel;
    }

    public FightResult setWinnerLevel(int winnerLevel) {
        this.winnerLevel = winnerLevel;
        return this;
    }

    public String getWinnerPicture() {
        return winnerPicture;
    }

    public FightResult setWinnerPicture(String winnerPicture) {
        this.winnerPicture = winnerPicture;
        return this;
    }

    public String getLoserName() {
        return loserName;
    }

    public FightResult setLoserName(String loserName) {
        this.loserName = loserName;
        return this;
    }

    public int getLoserLevel() {
        return loserLevel;
    }

    public FightResult setLoserLevel(int loserLevel) {
        this.loserLevel = loserLevel;
        return this;
    }

    public String getLoserPicture() {
        return loserPicture;
    }

    public FightResult setLoserPicture(String loserPicture) {
        this.loserPicture = loserPicture;
        return this;
    }

    public String getWinnerTeam() {
        return winnerTeam;
    }

    public FightResult setWinnerTeam(String winnerTeam) {
        this.winnerTeam = winnerTeam;
        return this;
    }

    public String getLoserTeam() {
        return loserTeam;
    }

    public FightResult setLoserTeam(String loserTeam) {
        this.loserTeam = loserTeam;
        return this;
    }
    // end::adocSkip[]
}
// end::adocBean[]
