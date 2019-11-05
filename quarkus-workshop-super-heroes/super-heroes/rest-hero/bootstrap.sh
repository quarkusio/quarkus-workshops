#!/usr/bin/env bash
# tag::adocSnippet[]
# If not already created, create the structure:
mkdir -p quarkus-workshop-super-heroes/super-heroes/rest-hero

cd quarkus-workshop-super-heroes/super-heroes/rest-hero
mvn io.quarkus:quarkus-maven-plugin:0.28.1:create \
    -DprojectGroupId=io.quarkus.workshop.super-heroes \
    -DprojectArtifactId=rest-hero \
    -DclassName="io.quarkus.workshop.superheroes.hero.HeroResource" \
    -Dpath="api/heroes"
# end::adocSnippet[]
