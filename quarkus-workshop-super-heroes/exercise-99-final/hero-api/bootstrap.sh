#!/usr/bin/env bash
mvn io.quarkus:quarkus-maven-plugin:0.23.1:create \
    -DprojectGroupId=io.quarkus.workshop.super-heroes \
    -DprojectArtifactId=hero-api \
    -DclassName="io.quarkus.workshop.superheroes.hero.HeroResource" \
    -Dpath="/api/heroes"
