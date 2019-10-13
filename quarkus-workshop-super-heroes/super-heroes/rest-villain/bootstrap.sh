#!/usr/bin/env bash
# tag::adocSnippet[]
mvn io.quarkus:quarkus-maven-plugin:0.24.0:create \
    -DprojectGroupId=io.quarkus.workshop.super-heroes \
    -DprojectArtifactId=rest-villain \
    -DclassName="io.quarkus.workshop.superheroes.villain.VillainResource" \
    -Dpath="/api/villains"
# end::adocSnippet[]
