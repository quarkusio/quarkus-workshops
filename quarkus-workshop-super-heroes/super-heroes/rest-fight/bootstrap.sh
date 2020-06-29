#!/usr/bin/env bash
# tag::adocSnippet[]
mvn io.quarkus:quarkus-maven-plugin:1.2.0.Final:create \
    -DprojectGroupId=io.quarkus.workshop.super-heroes \
    -DprojectArtifactId=rest-fight \
    -DclassName="io.quarkus.workshop.superheroes.fight.FightResource" \
    -Dpath="api/fights"
cd rest-fight
./mvnw quarkus:add-extension -Dextensions="jdbc-postgresql,hibernate-orm-panache,hibernate-validator,quarkus-resteasy-jsonb,smallrye-openapi,kafka"
# end::adocSnippet[]
