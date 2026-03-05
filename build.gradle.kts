plugins {
    id("org.jetbrains.gradle.plugin.idea-ext") version ("1.1.7")

//    alias(neoforged.plugins.common)
    alias(neoforged.plugins.moddev).apply(false)
}

subprojects {
    repositories {
        mavenLocal()

        maven("https://prmaven.neoforged.net/NeoForge/pr2879") {
            name = "NeoForge 26.1 Snapshot Builds" // https://github.com/neoforged/NeoForge/pull/2879
            content {
                includeModule("net.neoforged", "neoforge")
                includeModule("net.neoforged", "testframework")
            }
        }
    }
}