#!/usr/bin/env bash
# tag::adocSnippet[]
mvn io.quarkus:quarkus-maven-plugin:1.3.0.Final:create \
    -DprojectGroupId=io.quarkus.workshop.super-heroes \
    -DprojectArtifactId=event-statistics \
    -DclassName="io.quarkus.workshop.superheroes.statistics.StatisticResource" \
    -Dpath="api/stats" \
    -Dextensions="kafka, vertx, resteasy-jsonb, undertow-websockets"
cd event-statistics
# end::adocSnippet[]
