package io.quarkus.workshop.superheroes.fight.client;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

// tag::adocJavadoc[]

/**
 * POJO representing a Villain response from the Villain service
 */
// end::adocJavadoc[]
@Schema(description = "The villain fighting against the hero")
public class Villain {

    @NotNull
    public String name;
    @NotNull
    public int level;
    @NotNull
    public String picture;
    public String powers;

}
