pluginManagement {
    val quarkusPluginVersion: String by settings
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id("io.quarkus.extension") version quarkusPluginVersion
    }
}
rootProject.name="extension-version-parent"

include("runtime", "deployment")
// Give the modules the same artifact ids as the Maven build
project(":runtime").name = "extension-version"
project(":deployment").name = "extension-version-deployment"
