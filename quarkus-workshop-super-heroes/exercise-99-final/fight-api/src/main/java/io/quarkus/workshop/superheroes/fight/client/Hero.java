package io.quarkus.workshop.superheroes.fight.client;

public class Hero {

    private Long id;
    private String name;
    private String otherName;
    private int level;
    private String picture;
    private String powers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOtherName() {
        return otherName;
    }

    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getPowers() {
        return powers;
    }

    public void setPowers(String powers) {
        this.powers = powers;
    }

    @Override
    public String toString() {
        return "Hero{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", otherName='" + otherName + '\'' +
            ", level=" + level +
            ", picture='" + picture + '\'' +
            ", powers='" + powers + '\'' +
            '}';
    }
}
