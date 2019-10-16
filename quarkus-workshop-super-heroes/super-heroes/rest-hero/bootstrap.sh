#!/usr/bin/env bash
# tag::adocSnippet[]
cd quarkus-workshop-super-heroes/super-heroes/rest-hero
mvn io.quarkus:quarkus-maven-plugin:0.25.0:create \
    -DprojectGroupId=io.quarkus.workshop.super-heroes \
    -DprojectArtifactId=rest-hero \
    -DclassName="io.quarkus.workshop.superheroes.hero.HeroResource" \
    -Dpath="/api/heroes"
# end::adocSnippet[]
