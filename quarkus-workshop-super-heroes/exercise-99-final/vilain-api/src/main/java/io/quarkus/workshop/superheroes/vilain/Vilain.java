package io.quarkus.workshop.superheroes.vilain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

public class Vilain extends PanacheEntity {

    public String name;
    public String  otherName;
    public String pictureUrl;
    public String superpowers;
    public int level;

    public static Vilain findByName(String name){
        return find("name", name).firstResult();
    }
}
