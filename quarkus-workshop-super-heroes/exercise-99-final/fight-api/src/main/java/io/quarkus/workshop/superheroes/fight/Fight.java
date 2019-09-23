package io.quarkus.workshop.superheroes.fight;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.Instant;

@Entity
public class Fight extends PanacheEntity {

    public Instant fightDate;
    public String winnerName;
    public int winnerLevel;
    public String winnerPicture;
    public String loserName;
    public int loserLevel;
    public String loserPicture;

    @Column(columnDefinition = "TEXT")
    public String powers;

    @Override
    public String toString() {
        return "Fight{" +
            "id=" + id +
            ", fightDate=" + fightDate +
            ", winnerName='" + winnerName + '\'' +
            ", winnerLevel=" + winnerLevel +
            ", winnerPicture='" + winnerPicture + '\'' +
            ", loserName='" + loserName + '\'' +
            ", loserLevel=" + loserLevel +
            ", loserPicture='" + loserPicture + '\'' +
            ", powers='" + powers + '\'' +
            '}';
    }
}
