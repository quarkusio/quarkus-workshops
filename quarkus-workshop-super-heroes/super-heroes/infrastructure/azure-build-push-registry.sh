#!/bin/bash -e

# This script builds all the docker images and pushes them to Azure Registry
# See https://quarkus.io/quarkus-workshops/super-heroes/index-azure.html for details

source ./azure-setup-env-var.sh

cd ..


echo ">>> Compiling the microservices..."

# tag::adocCompiling[]
cd extension-version
mvn clean install
cd ..

cd rest-heroes
mvn clean package
cd ..

cd rest-villains
mvn clean package
cd ..

cd rest-fights
mvn clean package
cd ..

cd event-statistics
mvn clean package
cd ..
# end::adocCompiling[]


echo ">>> Building the microservices Docker images..."

# tag::adocBuilding[]
cd rest-heroes
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/rest-heroes .
cd ..

cd rest-villains
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/rest-villains ../..
cd ..

cd rest-fights
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/rest-fights .
cd ..

cd event-statistics
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/event-statistics .
cd ..

cd ui-super-heroes
docker build -f src/main/docker/Dockerfile.build-native -t quarkus/ui-super-heroes .
cd ..
# end::adocBuilding[]


echo ">>> Tagging the Docker images..."

REGISTRY_URL=$(az acr show \
  --resource-group "$RESOURCE_GROUP" \
  --name "$REGISTRY" \
  --query "loginServer" \
  --output tsv)

HEROES_IMAGE="${REGISTRY_URL}/${HEROES_APP}:${IMAGES_TAG}"
VILLAINS_IMAGE="${REGISTRY_URL}/${VILLAINS_APP}:${IMAGES_TAG}"
FIGHTS_IMAGE="${REGISTRY_URL}/${FIGHTS_APP}:${IMAGES_TAG}"
STATISTICS_IMAGE="${REGISTRY_URL}/${STATISTICS_APP}:${IMAGES_TAG}"
UI_IMAGE="${REGISTRY_URL}/${UI_APP}:${IMAGES_TAG}"

echo "> Tagging UI..." $UI_IMAGE
echo "> Tagging Statistics..." $STATISTICS_IMAGE
echo "> Tagging Fights..." $FIGHTS_IMAGE
echo "> Tagging Villains..." $VILLAINS_IMAGE
echo "> Tagging Heroes..." $HEROES_IMAGE

# tag::adocTaggingCore[]
docker tag quarkus/ui-super-heroes:1.0.0-SNAPSHOT   $UI_IMAGE
docker tag quarkus/rest-fights:1.0.0-SNAPSHOT       $FIGHTS_IMAGE
docker tag quarkus/rest-villains:1.0.0-SNAPSHOT     $VILLAINS_IMAGE
docker tag quarkus/rest-heroes:1.0.0-SNAPSHOT       $HEROES_IMAGE
# end::adocTaggingCore[]
# tag::adocTaggingStat[]
docker tag quarkus/event-statistics:1.0.0-SNAPSHOT  $STATISTICS_IMAGE
# end::adocTaggingStat[]
# tag::adocTaggingNarration[]
docker tag quarkus/rest-narration:1.0.0-SNAPSHOT    $NARRATION_IMAGE
# end::adocTaggingNarration[]

echo ">>> Logging into Container Registry..."

# tag::adocLogging[]
az acr login \
  --name "$REGISTRY"
# end::adocLogging[]


echo ">>> Pushing images into Container Registry..."

# tag::adocPushingCore[]
docker push $UI_IMAGE
docker push $FIGHTS_IMAGE
docker push $VILLAINS_IMAGE
docker push $HEROES_IMAGE
# end::adocPushingCore[]
# tag::adocPushingStat[]
docker push $STATISTICS_IMAGE
# end::adocPushingStat[]
# tag::adocPushingNarration[]
docker push $NARRATION_IMAGE
# end::adocPushingNarration[]


echo ">>> Listing images from the Container Registry..."

# tag::adocListing[]
az acr repository list \
  --name "$REGISTRY" \
  --output table
# end::adocListing[]


echo ">>> Showing images from the Container Registry..."

echo "> Showing Heroes..."
# tag::adocShowing[]
az acr repository show \
  --name "$REGISTRY" \
  --repository "$HEROES_APP"
# end::adocShowing[]

echo "> Showing Villains..."
az acr repository show \
  --name "$REGISTRY" \
  --repository "$VILLAINS_APP"
echo "> Showing Fights..."
az acr repository show \
  --name "$REGISTRY" \
  --repository "$FIGHTS_APP"
echo "> Showing Statistics..."
az acr repository show \
  --name "$REGISTRY" \
  --repository "$STATISTICS_APP"
echo "> Showing UI..."
az acr repository show \
  --name "$REGISTRY" \
  --repository "$UI_APP"
