// The Gradle counterpart of pom.xml: one ./gradlew build at this level builds every service.
// Each service is still a standalone build (with its own settings, properties and wrapper) that
// can be built from its own directory; this composite build just includes them.
//
// Rather than listing the services, include whichever ones are there, so the same file works in
// the starter, core and complete zips, which each contain a different set of them.
rootProject.name = "quarkus-workshop-super-heroes"

file("super-heroes").listFiles()
    ?.filter { it.resolve("settings.gradle.kts").exists() }
    ?.sortedBy { it.name }
    ?.forEach { includeBuild(it) }
