#!/bin/bash -e

# This script sets up the Azure Container Apps environment
# See https://quarkus.io/quarkus-workshops/super-heroes/index-azure.html for details

source ./azure-setup-env-var.sh

LOG_ANALYTICS_WORKSPACE_CLIENT_ID=`az monitor log-analytics workspace show  \
  --resource-group "$RESOURCE_GROUP" \
  --workspace-name "$LOG_ANALYTICS_WORKSPACE" \
  --query customerId  \
  --output tsv | tr -d '[:space:]'`

LOG_ANALYTICS_WORKSPACE_CLIENT_SECRET=`az monitor log-analytics workspace get-shared-keys \
  --resource-group "$RESOURCE_GROUP" \
  --workspace-name "$LOG_ANALYTICS_WORKSPACE" \
  --query primarySharedKey \
  --output tsv | tr -d '[:space:]'`


echo ">>> Creating Azure Container Apps environment..."

# tag::adocCreateACAEnv[]
az containerapp env create \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --tags system="$TAG" \
  --name "$CONTAINERAPPS_ENVIRONMENT" \
  --logs-workspace-id "$LOG_ANALYTICS_WORKSPACE_CLIENT_ID" \
  --logs-workspace-key "$LOG_ANALYTICS_WORKSPACE_CLIENT_SECRET"
# end::adocCreateACAEnv[]


echo ">>> Creating the Hero Postgres Database..."

# tag::adocCreatePostgresH[]
az postgres flexible-server create \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --tags system="$TAG" application="$HEROES_APP" \
  --name "$HEROES_DB" \
  --admin-user "$POSTGRES_DB_ADMIN" \
  --admin-password "$POSTGRES_DB_PWD" \
  --public all \
  --tier "$POSTGRES_TIER" \
  --sku-name "$POSTGRES_SKU" \
  --storage-size 32 \
  --version "$POSTGRES_DB_VERSION"
# end::adocCreatePostgresH[]


echo ">>> Creating the Villain Postgres Database..."

# tag::adocCreatePostgresV[]
az postgres flexible-server create \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --tags system="$TAG" application="$VILLAINS_APP" \
  --name "$VILLAINS_DB" \
  --admin-user "$POSTGRES_DB_ADMIN" \
  --admin-password "$POSTGRES_DB_PWD" \
  --public all \
  --tier "$POSTGRES_TIER" \
  --sku-name "$POSTGRES_SKU" \
  --storage-size 32 \
  --version "$POSTGRES_DB_VERSION"
# end::adocCreatePostgresV[]


echo ">>> Creating the Fight Postgres Database..."

# tag::adocCreatePostgresF[]
az postgres flexible-server create \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --tags system="$TAG" application="$FIGHTS_APP" \
  --name "$FIGHTS_DB" \
  --admin-user "$POSTGRES_DB_ADMIN" \
  --admin-password "$POSTGRES_DB_PWD" \
  --public all \
  --tier "$POSTGRES_TIER" \
  --sku-name "$POSTGRES_SKU" \
  --storage-size 32 \
  --version "$POSTGRES_DB_VERSION"
# end::adocCreatePostgresF[]


echo ">>> Creating the Hero Postgres Schema..."

# tag::adocCreateSchemaH[]
az postgres flexible-server db create \
    --resource-group "$RESOURCE_GROUP" \
    --server-name "$HEROES_DB" \
    --database-name "$HEROES_DB_SCHEMA"
# end::adocCreateSchemaH[]


echo ">>> Creating the Villain Postgres Schema..."

# tag::adocCreateSchemaV[]
az postgres flexible-server db create \
    --resource-group "$RESOURCE_GROUP" \
    --server-name "$VILLAINS_DB" \
    --database-name "$VILLAINS_DB_SCHEMA"
# end::adocCreateSchemaV[]


echo ">>> Creating the Fight Postgres Schema..."

# tag::adocCreateSchemaF[]
az postgres flexible-server db create \
    --resource-group "$RESOURCE_GROUP" \
    --server-name "$FIGHTS_DB" \
    --database-name "$FIGHTS_DB_SCHEMA"
# end::adocCreateSchemaF[]

cd ..

echo ">>> Initialize the Hero Database..."

# tag::adocInitTableH[]
az postgres flexible-server execute \
    --name "$HEROES_DB" \
    --admin-user "$POSTGRES_DB_ADMIN" \
    --admin-password "$POSTGRES_DB_PWD" \
    --database-name "$HEROES_DB_SCHEMA" \
    --file-path "infrastructure/db-init/initialize-tables-heroes.sql"
# end::adocInitTableH[]


echo ">>> Initialize the Villain Database..."

# tag::adocInitTableV[]
az postgres flexible-server execute \
    --name "$VILLAINS_DB" \
    --admin-user "$POSTGRES_DB_ADMIN" \
    --admin-password "$POSTGRES_DB_PWD" \
    --database-name "$VILLAINS_DB_SCHEMA" \
    --file-path "infrastructure/db-init/initialize-tables-villains.sql"
# end::adocInitTableV[]


echo ">>> Initialize the Fight Database..."

# tag::adocInitTableF[]
az postgres flexible-server execute \
    --name "$FIGHTS_DB" \
    --admin-user "$POSTGRES_DB_ADMIN" \
    --admin-password "$POSTGRES_DB_PWD" \
    --database-name "$FIGHTS_DB_SCHEMA" \
    --file-path "infrastructure/db-init/initialize-tables-fights.sql"
# end::adocInitTableF[]


echo ">>> Importing data to the Hero Database..."

# tag::adocImportDataH[]
az postgres flexible-server execute \
    --name "$HEROES_DB" \
    --admin-user "$POSTGRES_DB_ADMIN" \
    --admin-password "$POSTGRES_DB_PWD" \
    --database-name "$HEROES_DB_SCHEMA" \
    --file-path "rest-heroes/src/main/resources/import.sql"
# end::adocImportDataH[]


echo ">>> Importing data to the Villain Database..."

# tag::adocImportDataV[]
az postgres flexible-server execute \
    --name "$VILLAINS_DB" \
    --admin-user "$POSTGRES_DB_ADMIN" \
    --admin-password "$POSTGRES_DB_PWD" \
    --database-name "$VILLAINS_DB_SCHEMA" \
    --file-path "rest-villains/src/main/resources/import.sql"
# end::adocImportDataV[]


echo ">>> Importing data to the Fight Database..."

# tag::adocImportDataF[]
az postgres flexible-server execute \
    --name "$FIGHTS_DB" \
    --admin-user "$POSTGRES_DB_ADMIN" \
    --admin-password "$POSTGRES_DB_PWD" \
    --database-name "$FIGHTS_DB_SCHEMA" \
    --file-path "rest-fights/src/main/resources/import.sql"
# end::adocImportDataF[]


echo ">>> Retrieving data from the Hero Database..."

# tag::adocSelectDataH[]
az postgres flexible-server execute \
    --name "$HEROES_DB" \
    --admin-user "$POSTGRES_DB_ADMIN" \
    --admin-password "$POSTGRES_DB_PWD" \
    --database-name "$HEROES_DB_SCHEMA" \
    --querytext "select * from hero"
# end::adocSelectDataH[]


echo ">>> Retrieving data from the Villain Database..."

# tag::adocSelectDataV[]
az postgres flexible-server execute \
    --name "$VILLAINS_DB" \
    --admin-user "$POSTGRES_DB_ADMIN" \
    --admin-password "$POSTGRES_DB_PWD" \
    --database-name "$VILLAINS_DB_SCHEMA" \
    --querytext "select * from villain"
# end::adocSelectDataV[]


echo ">>> Retrieving data from the Fight Database..."

# tag::adocSelectDataF[]
az postgres flexible-server execute \
    --name "$FIGHTS_DB" \
    --admin-user "$POSTGRES_DB_ADMIN" \
    --admin-password "$POSTGRES_DB_PWD" \
    --database-name "$FIGHTS_DB_SCHEMA" \
    --querytext "select * from fight"
# end::adocSelectDataF[]


echo ">>> Creating Azure Event Hubs..."

# tag::adocCreateEventHub[]
az eventhubs namespace create \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --tags system="$TAG" application="$FIGHTS_APP" \
  --name "$KAFKA_NAMESPACE"
# end::adocCreateEventHub[]


echo ">>> Creating Azure Event Hubs Topic..."

# tag::adocCreateEventHubTopic[]
az eventhubs eventhub create \
  --resource-group "$RESOURCE_GROUP" \
  --name "$KAFKA_TOPIC" \
  --namespace-name "$KAFKA_NAMESPACE"
# end::adocCreateEventHubTopic[]


echo ">>> Retrieving Azure Event Hubs Connection String..."

# tag::adocEventHubConnection[]
KAFKA_CONNECTION_STRING=$(az eventhubs namespace authorization-rule keys list \
  --resource-group "$RESOURCE_GROUP" \
  --namespace-name "$KAFKA_NAMESPACE" \
  --name RootManageSharedAccessKey \
  --output json | jq -r .primaryConnectionString)

JAAS_CONFIG='org.apache.kafka.common.security.plain.PlainLoginModule required username="$ConnectionString" password="'
KAFKA_JAAS_CONFIG="${JAAS_CONFIG}${KAFKA_CONNECTION_STRING}\";"

echo $KAFKA_CONNECTION_STRING
echo $KAFKA_JAAS_CONFIG
# end::adocEventHubConnection[]
