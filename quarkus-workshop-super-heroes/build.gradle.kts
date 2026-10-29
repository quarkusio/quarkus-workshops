// Tasks run from here don't reach into included builds on their own, so forward the usual
// lifecycle tasks to every service that settings.gradle.kts included.
listOf("build", "clean").forEach { name ->
    tasks.register(name) {
        dependsOn(gradle.includedBuilds.map { it.task(":$name") })
    }
}
