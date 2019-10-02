package io.quarkus.workshop.superheroes.fight;

import io.quarkus.workshop.superheroes.fight.client.Hero;
import io.quarkus.workshop.superheroes.fight.client.Villain;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Schema(description="A fight between one hero and one villain")
public class Fighters {

    @NotNull
    private Hero hero;
    @NotNull
    private Villain villain;

    public Hero getHero() {
        return hero;
    }

    public void setHero(Hero hero) {
        this.hero = hero;
    }

    public Villain getVillain() {
        return villain;
    }

    public void setVillain(Villain villain) {
        this.villain = villain;
    }

    @Override
    public String toString() {
        return "Fighters{" +
            "hero=" + hero +
            ", villain=" + villain +
            '}';
    }
}
