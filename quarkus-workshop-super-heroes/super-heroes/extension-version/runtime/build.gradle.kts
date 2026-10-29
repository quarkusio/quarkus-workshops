plugins {
    id("io.quarkus.extension")
}

dependencies {
    implementation("io.quarkus:quarkus-arc")
}

quarkusExtension {
    deploymentModule.set("extension-version-deployment")
}
