#!/usr/bin/env bash
# tag::adocSnippet[]
cd quarkus-workshop-super-heroes/super-heroes
mvn io.quarkus:quarkus-maven-plugin:1.9.2.Final:create \
    -DprojectGroupId=io.quarkus.workshop.super-heroes \
    -DprojectArtifactId=rest-hero \
    -DclassName="io.quarkus.workshop.superheroes.hero.HeroResource" \
    -Dpath="api/heroes"
# end::adocSnippet[]
