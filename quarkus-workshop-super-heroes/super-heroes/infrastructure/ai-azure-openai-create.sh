#!/usr/bin/env bash

# tag::adocSetup[]
echo "Setting up environment variables..."
echo "----------------------------------"
PROJECT="<give-your-project-a-name>"
RESOURCE_GROUP="rg-$PROJECT"
LOCATION="eastus"
TAG="$PROJECT"
COGNITIVE_SERVICE="cognit-$PROJECT"
COGNITIVE_DEPLOYMENT="gpt35turbo"

echo "Creating the resource group..."
echo "------------------------------"
az group create \
  --name "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --tags system="$TAG"

# tag::adocSkip[]
# To know which SKUs are available, run:
az cognitiveservices account list-skus \
  --location "$LOCATION"
# To know which kinds are available, run:
az cognitiveservices account list-kinds
# end::adocSkip[]

echo "Creating the Cognitive Service..."
echo "---------------------------------"
az cognitiveservices account create \
  --name "$COGNITIVE_SERVICE" \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --custom-domain "$COGNITIVE_SERVICE" \
  --tags system="$TAG" \
  --kind "OpenAI" \
  --sku "S0"

# tag::adocSkip[]
# To know which models are available, run:
az cognitiveservices account list-models \
  --name "$COGNITIVE_SERVICE" \
  --resource-group "$RESOURCE_GROUP"
# end::adocSkip[]

echo "Deploying the model..."
echo "----------------------"
az cognitiveservices account deployment create \
  --name "$COGNITIVE_SERVICE" \
  --resource-group "$RESOURCE_GROUP" \
  --deployment-name "$COGNITIVE_DEPLOYMENT" \
  --model-name "gpt-35-turbo" \
  --model-version "0301"  \
  --model-format "OpenAI" \
  --sku-capacity 1 \
  --sku-name "Standard"

# tag::adocSkip[]
echo "Getting the model..."
echo "--------------------"
az cognitiveservices account deployment show \
  --name "$COGNITIVE_SERVICE" \
  --resource-group "$RESOURCE_GROUP" \
  --deployment-name "$COGNITIVE_DEPLOYMENT"
# end::adocSkip[]
# end::adocSetup[]

# tag::adocProperties[]
echo "Storing the key and endpoint in environment variables..."
echo "--------------------------------------------------------"
AZUREOPENAI_KEY=$(
  az cognitiveservices account keys list \
    --name "$COGNITIVE_SERVICE" \
    --resource-group "$RESOURCE_GROUP" \
    | jq -r .key1
)
AZUREOPENAI_ENDPOINT=$(
  az cognitiveservices account show \
    --name "$COGNITIVE_SERVICE" \
    --resource-group "$RESOURCE_GROUP" \
    | jq -r .properties.endpoint
)

# Set the properties
echo "--------------------------------------------------"
echo "The following properties can be copied to either the rest-narration/src/main/resources/conf.properties or to the ~/.sk/conf.properties file:"
echo "--------------------------------------------------"
echo "client.azureopenai.key=$AZUREOPENAI_KEY"
echo "client.azureopenai.endpoint=$AZUREOPENAI_ENDPOINT"
echo "client.azureopenai.deploymentname=$COGNITIVE_DEPLOYMENT"
# end::adocProperties[]
