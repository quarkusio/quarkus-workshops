package io.quarkus.workshop.superheroes.villain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Random;

/**
 * JPA entity class for a Villain. Re-used in the API layer.
 */
@Entity
public class Villain extends PanacheEntity {
    @NotNull
    @Size(min = 3, max = 50)
    public String name;

    public String otherName;

    @NotNull
    @Min(1)
    public int level;

    public String picture;

    @Column(columnDefinition = "TEXT")
    public String powers;

    public static Villain findRandom() {
        long countVillains = count();
        Random random = new Random();
        int randomVillain = random.nextInt((int) countVillains);
        return findAll().page(randomVillain, 1).firstResult();
    }

    @Override
    /* prettier-ignore */
    public String toString() {
        return (
            "Villain{" +
            "id=" +
            id +
            ", name='" +
            name +
            '\'' +
            ", otherName='" +
            otherName +
            '\'' +
            ", level=" +
            level +
            ", picture='" +
            picture +
            '\'' +
            ", powers='" +
            powers +
            '\'' +
            '}'
        );
    }
}
