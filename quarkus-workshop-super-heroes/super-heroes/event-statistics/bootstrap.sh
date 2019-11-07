#!/usr/bin/env bash
# tag::adocSnippet[]
mvn io.quarkus:quarkus-maven-plugin:1.0.0.CR1:create \
    -DprojectGroupId=io.quarkus.workshop.super-heroes \
    -DprojectArtifactId=event-statistics \
    -DclassName="io.quarkus.workshop.superheroes.statistics.StatisticResource" \
    -Dpath="api/stats" \
    -Dextensions="kafka, vertx, resteasy-jsonb, undertow-websockets"
# end::adocSnippet[]
