package io.quarkus.workshop.superheroes.statistics;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
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
}
