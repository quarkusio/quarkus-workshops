#!/bin/bash -e

echo ">>> Setting environment variables..."

# tag::adocEnvironmentVariables[]
PROJECT="super-heroes"
RESOURCE_GROUP="rg-${PROJECT}"
LOCATION="eastus"
TAG=$PROJECT

LOG_ANALYTICS_WORKSPACE="log-${PROJECT}"

UNIQUE_IDENTIFIER=$(whoami)
REGISTRY="registrysuperheroes"$UNIQUE_IDENTIFIER
IMAGES_TAG="1.0"
# end::adocEnvironmentVariables[]

# tag::adocEnvironmentVariables2[]
# Container Apps
CONTAINERAPPS_ENVIRONMENT="env-${PROJECT}"

# Postgres
POSTGRES_DB_ADMIN="superheroesadmin"
POSTGRES_DB_PWD="super-heroes-p#ssw0rd-12046"
POSTGRES_DB_VERSION="14"
POSTGRES_SKU="Standard_B1ms"
POSTGRES_TIER="Burstable"

# Kafka
KAFKA_NAMESPACE="fights-kafka-$UNIQUE_IDENTIFIER"
KAFKA_TOPIC="fights"
KAFKA_BOOTSTRAP_SERVERS="$KAFKA_NAMESPACE.servicebus.windows.net:9093"

# Heroes
HEROES_APP="heroes-app"
HEROES_DB="heroes-db-$UNIQUE_IDENTIFIER"
HEROES_IMAGE="${REGISTRY_URL}/${HEROES_APP}:${IMAGES_TAG}"
HEROES_DB_SCHEMA="heroes"
HEROES_DB_CONNECT_STRING="postgresql://${HEROES_DB}.postgres.database.azure.com:5432/${HEROES_DB_SCHEMA}?ssl=true&sslmode=require"

# Villains
VILLAINS_APP="villains-app"
VILLAINS_DB="villains-db-$UNIQUE_IDENTIFIER"
VILLAINS_IMAGE="${REGISTRY_URL}/${VILLAINS_APP}:${IMAGES_TAG}"
VILLAINS_DB_SCHEMA="villains"
VILLAINS_DB_CONNECT_STRING="jdbc:postgresql://${VILLAINS_DB}.postgres.database.azure.com:5432/${VILLAINS_DB_SCHEMA}?ssl=true&sslmode=require"

# Fights
FIGHTS_APP="fights-app"
FIGHTS_DB="fights-db-$UNIQUE_IDENTIFIER"
FIGHTS_IMAGE="${REGISTRY_URL}/${FIGHTS_APP}:${IMAGES_TAG}"
FIGHTS_DB_SCHEMA="fights"
FIGHTS_DB_CONNECT_STRING="jdbc:postgresql://${FIGHTS_DB}.postgres.database.azure.com:5432/${FIGHTS_DB_SCHEMA}?ssl=true&sslmode=require"

# Statistics
STATISTICS_APP="statistics-app"
STATISTICS_IMAGE="${REGISTRY_URL}/${STATISTICS_APP}:${IMAGES_TAG}"

# UI
UI_APP="super-heroes-ui"
UI_IMAGE="${REGISTRY_URL}/${UI_APP}:${IMAGES_TAG}"
# end::adocEnvironmentVariables2[]
