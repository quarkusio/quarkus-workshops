#!/bin/bash -e

# This script sets up the Azure environment
# See https://quarkus.io/quarkus-workshops/super-heroes/index-azure.html for details

echo ">>> Retrieving Azure account..."

# tag::adocAzAccountShow[]
az account show
# end::adocAzAccountShow[]

source ./azure-setup-env-var.sh

echo ">>> Creating resource group..."

# tag::adocCreateResourceGroup[]
az group create \
  --name "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --tags system="$TAG"
# end::adocCreateResourceGroup[]


echo ">>> Creating log analytics workspace..."

# tag::adocCreateLogAnalytics[]
az monitor log-analytics workspace create \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --tags system="$TAG" \
  --workspace-name "$LOG_ANALYTICS_WORKSPACE"
# end::adocCreateLogAnalytics[]


echo ">>> Retrieving log analytics secrets..."

# tag::adocLogAnalyticsSecrets[]
LOG_ANALYTICS_WORKSPACE_CLIENT_ID=`az monitor log-analytics workspace show  \
  --resource-group "$RESOURCE_GROUP" \
  --workspace-name "$LOG_ANALYTICS_WORKSPACE" \
  --query customerId  \
  --output tsv | tr -d '[:space:]'`

echo $LOG_ANALYTICS_WORKSPACE_CLIENT_ID

LOG_ANALYTICS_WORKSPACE_CLIENT_SECRET=`az monitor log-analytics workspace get-shared-keys \
  --resource-group "$RESOURCE_GROUP" \
  --workspace-name "$LOG_ANALYTICS_WORKSPACE" \
  --query primarySharedKey \
  --output tsv | tr -d '[:space:]'`

echo $LOG_ANALYTICS_WORKSPACE_CLIENT_SECRET
# end::adocLogAnalyticsSecrets[]


echo ">>> Creating Azure Container Registry..."

# tag::adocCreateContainerRegistry[]
az acr create \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --tags system="$TAG" \
  --name "$REGISTRY" \
  --workspace "$LOG_ANALYTICS_WORKSPACE" \
  --sku Premium \
  --admin-enabled true
# end::adocCreateContainerRegistry[]


echo ">>> Allowing anonymous pull to Container Registry..."

# tag::adocAnonymousContainerRegistry[]
az acr update \
  --resource-group "$RESOURCE_GROUP" \
  --name "$REGISTRY" \
  --anonymous-pull-enabled true
# end::adocAnonymousContainerRegistry[]


echo ">>> Retrieving Container Registry URL..."

# tag::adocContainerRegistryURL[]
REGISTRY_URL=$(az acr show \
  --resource-group "$RESOURCE_GROUP" \
  --name "$REGISTRY" \
  --query "loginServer" \
  --output tsv)

echo $REGISTRY_URL
# end::adocContainerRegistryURL[]
