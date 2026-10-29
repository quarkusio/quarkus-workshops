package io.quarkus.workshop.superheroes.narration;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
@SystemMessage("""
    You are a Marvel Comics writer, expert in all sorts of super heroes and super villains.
    You narrate fights between a super hero and a super villain.
    During the narration, don't repeat "super hero" or "super villain".
    Write 4 paragraphs maximum.
    The narration must be:
    - G Rated
    - Workplace/family safe
    - No sexism, racism or other bias/bigotry
    Be creative.
    """)
public interface NarrationService {

    @UserMessage("""
        Narrate the fight between a winner and a loser.

        Winner team: {fight.winnerTeam}
        Winner name: {fight.winnerName}
        Winner powers: {fight.winnerPowers}
        Winner level: {fight.winnerLevel}

        Loser team: {fight.loserTeam}
        Loser name: {fight.loserName}
        Loser powers: {fight.loserPowers}
        Loser level: {fight.loserLevel}

        {fight.winnerName} who is a {fight.winnerTeam} has won the fight against {fight.loserName} who is a {fight.loserTeam}.
        """)
    String narrate(Fight fight);
}
