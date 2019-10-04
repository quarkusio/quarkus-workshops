#!/usr/bin/env bash
# tag::adocSnippet[]
mvn io.quarkus:quarkus-maven-plugin:0.23.1:create \
    -DprojectGroupId=io.quarkus.workshop.super-heroes \
    -DprojectArtifactId=villain-api \
    -DclassName="io.quarkus.workshop.superheroes.villain.VillainResource" \
    -Dpath="/api/villains"
# end::adocSnippet[]
