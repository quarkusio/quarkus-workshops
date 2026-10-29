package io.quarkus.workshop.superheroes.fight.client;

public class DefaultTestVillain extends Villain {
    public static final String DEFAULT_VILLAIN_NAME = "Super Chocolatine";
    public static final String DEFAULT_VILLAIN_PICTURE = "super_chocolatine.png";
    public static final String DEFAULT_VILLAIN_POWERS = "does not eat pain au chocolat";
    public static final int DEFAULT_VILLAIN_LEVEL = 42;

    public static final DefaultTestVillain INSTANCE = new DefaultTestVillain();

    private DefaultTestVillain() {
        this.name = DEFAULT_VILLAIN_NAME;
        this.picture = DEFAULT_VILLAIN_PICTURE;
        this.powers = DEFAULT_VILLAIN_POWERS;
        this.level = DEFAULT_VILLAIN_LEVEL;
    }
}
