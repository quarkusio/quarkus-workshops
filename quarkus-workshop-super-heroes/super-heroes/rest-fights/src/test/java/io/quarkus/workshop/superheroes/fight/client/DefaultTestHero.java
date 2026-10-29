package io.quarkus.workshop.superheroes.fight.client;

public class DefaultTestHero extends Hero {
    public static final String DEFAULT_HERO_NAME = "Super Baguette";
    public static final String DEFAULT_HERO_PICTURE = "super_baguette.png";
    public static final String DEFAULT_HERO_POWERS = "eats baguette really quickly";
    public static final int DEFAULT_HERO_LEVEL = 42;

    public static final DefaultTestHero INSTANCE = new DefaultTestHero();

    private DefaultTestHero() {
        this.name = DEFAULT_HERO_NAME;
        this.picture = DEFAULT_HERO_PICTURE;
        this.powers = DEFAULT_HERO_POWERS;
        this.level = DEFAULT_HERO_LEVEL;
    }
}
