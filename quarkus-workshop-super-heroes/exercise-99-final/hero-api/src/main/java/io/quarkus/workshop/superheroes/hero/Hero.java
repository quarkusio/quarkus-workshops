package io.quarkus.workshop.superheroes.hero;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

public class Hero extends PanacheEntity {

    public String name;
    public String  otherName;
    public String pictureUrl;
    public String superpowers;
    public int level;

    public static Hero findByName(String name){
        return find("name", name).firstResult();
    }
}
