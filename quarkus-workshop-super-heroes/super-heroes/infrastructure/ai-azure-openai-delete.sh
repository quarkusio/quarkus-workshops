#!/usr/bin/env bash

echo "Setting up environment variables..."
echo "----------------------------------"
PROJECT="<give-your-project-a-name>"
RESOURCE_GROUP="rg-$PROJECT"
COGNITIVE_SERVICE="cognit-$PROJECT"
COGNITIVE_DEPLOYMENT="deploy-$PROJECT"
LOCATION="eastus"
TAG="$PROJECT"

# tag::adocDelete[]
# Clean up
az group delete \
  --name "$RESOURCE_GROUP" \
  --yes

az cognitiveservices account purge \
  --name "$COGNITIVE_SERVICE" \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION"

az cognitiveservices account delete \
  --name "$COGNITIVE_SERVICE" \
  --resource-group "$RESOURCE_GROUP"
# end::adocDelete[]
