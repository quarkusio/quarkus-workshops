package io.quarkus.workshop.superheroes.fight.client;

import io.quarkus.test.Mock;

import javax.enterprise.context.ApplicationScoped;

@Mock
@ApplicationScoped
public class MockHeroService implements HeroService {

    public static final String DEFAULT_HERO_NAME = "Super Baguette";
    public static final String DEFAULT_HERO_OTHER_NAME = "Super Baguette Tradition";
    public static final String DEFAULT_HERO_PICTURE = "super_baguette.png";
    public static final String DEFAULT_HERO_POWERS = "eats baguette really quickly";
    public static final int DEFAULT_HERO_LEVEL = 42;

    @Override
    public Hero findRandomHero() {
        Hero hero = new Hero();
        hero.setName(DEFAULT_HERO_NAME);
        hero.setOtherName(DEFAULT_HERO_OTHER_NAME);
        hero.setPicture(DEFAULT_HERO_PICTURE);
        hero.setPowers(DEFAULT_HERO_POWERS);
        hero.setLevel(DEFAULT_HERO_LEVEL);
        return hero;
    }
}
