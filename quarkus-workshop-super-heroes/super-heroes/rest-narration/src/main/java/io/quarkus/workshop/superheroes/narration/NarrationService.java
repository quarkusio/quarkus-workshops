package io.quarkus.workshop.superheroes.narration;

public interface NarrationService {
    String narrate(Fight fight) throws Exception;
}
