#!/usr/bin/env bash
# tag::adocSnippet[]
./mvnw io.quarkus:quarkus-maven-plugin:0.25.0:create \
    -DprojectGroupId=io.quarkus.workshop.super-heroes \
    -DprojectArtifactId=rest-villain \
    -DclassName="io.quarkus.workshop.superheroes.villain.VillainResource" \
    -Dpath="/api/villains"
./mvnw quarkus:add-extension -Dextensions="jdbc-postgresql,hibernate-orm-panache,hibernate-validator,quarkus-resteasy-jsonb,openapi"
# end::adocSnippet[]
