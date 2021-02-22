package io.quarkus.workshop.superheroes.statistics;

import java.time.Instant;


public class Fight {

    public Instant fightDate;
    public String winnerName;
    public int winnerLevel;
    public String winnerPicture;
    public String loserName;
    public int loserLevel;
    public String loserPicture;
    public String winnerTeam;
    public String loserTeam;

    @Override
    public String toString() {
        return "Fight{" +
           "fightDate=" + fightDate +
            ", winnerName='" + winnerName + '\'' +
            ", winnerLevel=" + winnerLevel +
            ", winnerPicture='" + winnerPicture + '\'' +
            ", winnerTeam='" + winnerTeam + '\'' +
            ", loserName='" + loserName + '\'' +
            ", loserLevel=" + loserLevel +
            ", loserPicture='" + loserPicture + '\'' +
            ", loserTeam='" + loserTeam + '\'' +
            '}';
    }
}
