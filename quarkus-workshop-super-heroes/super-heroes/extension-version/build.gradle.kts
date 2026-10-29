plugins {
    base
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

subprojects {
    apply(plugin = "java-library")
    // Publishing to the local Maven repository makes the extension available to the microservices
    apply(plugin = "maven-publish")

    group = "io.quarkus.workshop.super-heroes"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        "implementation"(platform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }
}

// The workshop's root build runs build and clean on this project, so pass them on to the modules
tasks.named("build") {
    dependsOn(subprojects.map { "${it.path}:build" })
}
tasks.named("clean") {
    dependsOn(subprojects.map { "${it.path}:clean" })
}
