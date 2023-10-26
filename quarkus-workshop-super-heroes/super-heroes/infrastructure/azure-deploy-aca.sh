#!/bin/bash -e

# This script deploys the applications to Azure Container Apps
# See https://quarkus.io/quarkus-workshops/super-heroes/index-azure.html for details

source ./azure-setup-env-var.sh

REGISTRY_URL=$(az acr show \
  --resource-group "$RESOURCE_GROUP" \
  --name "$REGISTRY" \
  --query "loginServer" \
  --output tsv)

HEROES_IMAGE="${REGISTRY_URL}/${HEROES_APP}:${IMAGES_TAG}"
VILLAINS_IMAGE="${REGISTRY_URL}/${VILLAINS_APP}:${IMAGES_TAG}"
FIGHTS_IMAGE="${REGISTRY_URL}/${FIGHTS_APP}:${IMAGES_TAG}"
STATISTICS_IMAGE="${REGISTRY_URL}/${STATISTICS_APP}:${IMAGES_TAG}"
NARRATION_IMAGE="${REGISTRY_URL}/${NARRATION_APP}:${IMAGES_TAG}"
UI_IMAGE="${REGISTRY_URL}/${UI_APP}:${IMAGES_TAG}"


echo ">>> Creating Hero app in Azure Container Apps..."

# tag::adocCreateAppHero[]
az containerapp create \
  --resource-group "$RESOURCE_GROUP" \
  --tags system="$TAG" application="$HEROES_APP" \
  --image "$HEROES_IMAGE" \
  --name "$HEROES_APP" \
  --environment "$CONTAINERAPPS_ENVIRONMENT" \
  --ingress external \
  --target-port 8083 \
  --min-replicas 0 \
  --env-vars QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=validate \
             QUARKUS_HIBERNATE_ORM_SQL_LOAD_SCRIPT=no-file \
             QUARKUS_DATASOURCE_USERNAME="$POSTGRES_DB_ADMIN" \
             QUARKUS_DATASOURCE_PASSWORD="$POSTGRES_DB_PWD" \
             QUARKUS_DATASOURCE_REACTIVE_URL="$HEROES_DB_CONNECT_STRING"
# end::adocCreateAppHero[]


echo ">>> Retrieving the URL of Hero app..."

# tag::adocAppHeroURL[]
HEROES_URL="https://$(az containerapp ingress show \
    --resource-group "$RESOURCE_GROUP" \
    --name "$HEROES_APP" \
    --output json | jq -r .fqdn)"

echo $HEROES_URL
# end::adocAppHeroURL[]


echo ">>> Retrieving logs of Hero app..."

# tag::adocAppHeroLogs[]
az monitor log-analytics query \
  --workspace $LOG_ANALYTICS_WORKSPACE_CLIENT_ID \
  --analytics-query "ContainerAppConsoleLogs_CL | where ContainerAppName_s == '$HEROES_APP' | project ContainerAppName_s, Log_s, TimeGenerated " \
  --output table
# end::adocAppHeroLogs[]



echo ">>> Creating Villain app in Azure Container Apps..."

# tag::adocCreateAppVillain[]
az containerapp create \
  --resource-group "$RESOURCE_GROUP" \
  --tags system="$TAG" application="$VILLAINS_APP" \
  --image "$VILLAINS_IMAGE" \
  --name "$VILLAINS_APP" \
  --environment "$CONTAINERAPPS_ENVIRONMENT" \
  --ingress external \
  --target-port 8084 \
  --min-replicas 0 \
  --env-vars QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=validate \
             QUARKUS_HIBERNATE_ORM_SQL_LOAD_SCRIPT=no-file \
             QUARKUS_DATASOURCE_USERNAME="$POSTGRES_DB_ADMIN" \
             QUARKUS_DATASOURCE_PASSWORD="$POSTGRES_DB_PWD" \
             QUARKUS_DATASOURCE_JDBC_URL="$VILLAINS_DB_CONNECT_STRING"
# end::adocCreateAppVillain[]


echo ">>> Retrieving the URL of Villain app..."

# tag::adocAppVillainURL[]
VILLAINS_URL="https://$(az containerapp ingress show \
    --resource-group "$RESOURCE_GROUP" \
    --name "$VILLAINS_APP" \
    --output json | jq -r .fqdn)"

echo $VILLAINS_URL
# end::adocAppVillainURL[]


echo ">>> Retrieving logs of Villain app..."

# tag::adocAppVillainLogs[]
az monitor log-analytics query \
  --workspace $LOG_ANALYTICS_WORKSPACE_CLIENT_ID \
  --analytics-query "ContainerAppConsoleLogs_CL | where ContainerAppName_s == '$VILLAINS_APP' | project ContainerAppName_s, Log_s, TimeGenerated " \
  --output table
# end::adocAppVillainLogs[]


echo ">>> Creating Narration app in Azure Container Apps..."

# tag::adocCreateAppNarration[]
az containerapp create \
  --resource-group "$RESOURCE_GROUP" \
  --tags system="$TAG" application="$NARRATION_APP" \
  --image "$NARRATION_IMAGE" \
  --name "$NARRATION_APP" \
  --environment "$CONTAINERAPPS_ENVIRONMENT" \
  --ingress external \
  --target-port 8086 \
  --min-replicas 0
# end::adocCreateAppNarration[]


echo ">>> Retrieving the URL of Narration app..."

# tag::adocAppNarrationURL[]
NARRATION_URL="https://$(az containerapp ingress show \
    --resource-group "$RESOURCE_GROUP" \
    --name "$NARRATION_APP" \
    --output json | jq -r .fqdn)"

echo $NARRATION_URL
# end::adocAppNarrationURL[]


echo ">>> Retrieving logs of Narration app..."

# tag::adocAppNarrationLogs[]
az monitor log-analytics query \
  --workspace $LOG_ANALYTICS_WORKSPACE_CLIENT_ID \
  --analytics-query "ContainerAppConsoleLogs_CL | where ContainerAppName_s == '$NARRATION_APP' | project ContainerAppName_s, Log_s, TimeGenerated " \
  --output table
# end::adocAppNarrationLogs[]


echo ">>> Creating Statistics app in Azure Container Apps..."

# tag::adocCreateAppStatistics[]
az containerapp create \
  --resource-group "$RESOURCE_GROUP" \
  --tags system="$TAG" application="$STATISTICS_APP" \
  --image "$STATISTICS_IMAGE" \
  --name "$STATISTICS_APP" \
  --environment "$CONTAINERAPPS_ENVIRONMENT" \
  --ingress external \
  --target-port 8085 \
  --min-replicas 0 \
  --env-vars KAFKA_BOOTSTRAP_SERVERS="$KAFKA_BOOTSTRAP_SERVERS" \
             KAFKA_SECURITY_PROTOCOL=SASL_SSL \
             KAFKA_SASL_MECHANISM=PLAIN \
             KAFKA_SASL_JAAS_CONFIG="$KAFKA_JAAS_CONFIG"
# end::adocCreateAppStatistics[]


echo ">>> Retrieving the URL of Statistics app..."

# tag::adocAppStatisticsURL[]
STATISTICS_URL="https://$(az containerapp ingress show \
    --resource-group "$RESOURCE_GROUP" \
    --name "$STATISTICS_APP" \
    --output json | jq -r .fqdn)"

echo $STATISTICS_URL
# end::adocAppStatisticsURL[]


echo ">>> Retrieving logs of Statistics app..."

# tag::adocAppStatisticsLogs[]
az monitor log-analytics query \
  --workspace $LOG_ANALYTICS_WORKSPACE_CLIENT_ID \
  --analytics-query "ContainerAppConsoleLogs_CL | where ContainerAppName_s == '$STATISTICS_APP' | project ContainerAppName_s, Log_s, TimeGenerated " \
  --output table
# end::adocAppStatisticsLogs[]


echo ">>> Creating Fight app in Azure Container Apps..."

# tag::adocCreateAppFight[]
az containerapp create \
  --resource-group "$RESOURCE_GROUP" \
  --tags system="$TAG" application="$FIGHTS_APP" \
  --image "$FIGHTS_IMAGE" \
  --name "$FIGHTS_APP" \
  --environment "$CONTAINERAPPS_ENVIRONMENT" \
  --ingress external \
  --target-port 8082 \
  --min-replicas 0 \
  --env-vars QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=validate \
             QUARKUS_HIBERNATE_ORM_SQL_LOAD_SCRIPT=no-file \
             QUARKUS_DATASOURCE_USERNAME="$POSTGRES_DB_ADMIN" \
             QUARKUS_DATASOURCE_PASSWORD="$POSTGRES_DB_PWD" \
             QUARKUS_DATASOURCE_JDBC_URL="$FIGHTS_DB_CONNECT_STRING" \
             KAFKA_BOOTSTRAP_SERVERS="$KAFKA_BOOTSTRAP_SERVERS" \
             KAFKA_SECURITY_PROTOCOL=SASL_SSL \
             KAFKA_SASL_MECHANISM=PLAIN \
             KAFKA_SASL_JAAS_CONFIG="$KAFKA_JAAS_CONFIG" \
             QUARKUS_REST_CLIENT_HERO_URL="$HEROES_URL" \
             QUARKUS_REST_CLIENT_VILLAIN_URL="$VILLAINS_URL" \
             QUARKUS_REST_CLIENT_NARRATION_URL="$NARRATION_URL"
# end::adocCreateAppFight[]


echo ">>> Retrieving the URL of Fight app..."

# tag::adocAppFightURL[]
FIGHTS_URL="https://$(az containerapp ingress show \
    --resource-group "$RESOURCE_GROUP" \
    --name "$FIGHTS_APP" \
    --output json | jq -r .fqdn)"

echo $FIGHTS_URL
# end::adocAppFightURL[]


echo ">>> Retrieving logs of Fight app..."

# tag::adocAppFightLogs[]
az monitor log-analytics query \
  --workspace $LOG_ANALYTICS_WORKSPACE_CLIENT_ID \
  --analytics-query "ContainerAppConsoleLogs_CL | where ContainerAppName_s == '$FIGHTS_APP' | project ContainerAppName_s, Log_s, TimeGenerated " \
  --output table
# end::adocAppFightLogs[]


echo ">>> Creating UI app in Azure Container Apps..."

# tag::adocCreateAppUI[]
az containerapp create \
  --resource-group "$RESOURCE_GROUP" \
  --tags system="$TAG" application="$UI_APP" \
  --image "$UI_IMAGE" \
  --name "$UI_APP" \
  --environment "$CONTAINERAPPS_ENVIRONMENT" \
  --ingress external \
  --target-port 8080 \
  --env-vars API_BASE_URL="$FIGHTS_URL"
# end::adocCreateAppUI[]


echo ">>> Retrieving the URL of UI app..."

# tag::adocAppUIURL[]
UI_URL="https://$(az containerapp ingress show \
    --resource-group "$RESOURCE_GROUP" \
    --name "$UI_APP" \
    --output json | jq -r .fqdn)"

echo $UI_URL
# end::adocAppUIURL[]


echo ">>> Retrieving logs of UI app..."

# tag::adocAppUILogs[]
az monitor log-analytics query \
  --workspace $LOG_ANALYTICS_WORKSPACE_CLIENT_ID \
  --analytics-query "ContainerAppConsoleLogs_CL | where ContainerAppName_s == '$UI_APP' | project ContainerAppName_s, Log_s, TimeGenerated " \
  --output table
# end::adocAppUILogs[]
