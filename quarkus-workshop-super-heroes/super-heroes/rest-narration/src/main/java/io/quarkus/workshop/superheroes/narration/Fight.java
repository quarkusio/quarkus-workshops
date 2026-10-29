package io.quarkus.workshop.superheroes.narration;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The fight that is narrated")
public class Fight {

    public String winnerName;
    public int winnerLevel;
    public String winnerPowers;
    public String loserName;
    public int loserLevel;
    public String loserPowers;
    public String winnerTeam;
    public String loserTeam;
}
