package io.quarkus.workshop.superheroes.statistics;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class FightDeserializer extends JsonbDeserializer<Fight> {

    public FightDeserializer() {
        super(Fight.class);
    }
}
