#!/usr/bin/env bash
# tag::adocSnippet[]
mvn io.quarkus:quarkus-maven-plugin:1.9.2.Final:create \
    -DprojectGroupId=io.quarkus.workshop.super-heroes \
    -DprojectArtifactId=rest-fight \
    -DclassName="io.quarkus.workshop.superheroes.fight.FightResource" \
    -Dpath="api/fights" \
    -Dextensions="jdbc-postgresql,hibernate-orm-panache,hibernate-validator,quarkus-resteasy-jsonb,smallrye-openapi,kafka"
cd rest-fight
# end::adocSnippet[]
