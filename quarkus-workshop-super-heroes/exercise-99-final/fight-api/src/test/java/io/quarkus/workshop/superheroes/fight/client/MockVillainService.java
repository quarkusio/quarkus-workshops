package io.quarkus.workshop.superheroes.fight.client;

import io.quarkus.test.Mock;

import javax.enterprise.context.ApplicationScoped;

@Mock
@ApplicationScoped
public class MockVillainService implements VillainService {

    public static final String DEFAULT_VILLAIN_NAME = "Super Chocolatine";
    public static final String DEFAULT_VILLAIN_OTHER_NAME = "Super Chocolatine chocolate in";
    public static final String DEFAULT_VILLAIN_PICTURE = "super_chocolatine.png";
    public static final String DEFAULT_VILLAIN_POWERS = "does not eat pain au chocolat";
    public static final int DEFAULT_VILLAIN_LEVEL = 42;

    @Override
    public Villain findRandomVillain() {
        Villain villain = new Villain();
        villain.setName(DEFAULT_VILLAIN_NAME);
        villain.setOtherName(DEFAULT_VILLAIN_OTHER_NAME);
        villain.setPicture(DEFAULT_VILLAIN_PICTURE);
        villain.setPowers(DEFAULT_VILLAIN_POWERS);
        villain.setLevel(DEFAULT_VILLAIN_LEVEL);
        return villain;
    }
}
