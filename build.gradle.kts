import net.neoforged.moddevgradle.dsl.NeoForgeExtension

plugins {
    id("org.jetbrains.gradle.plugin.idea-ext") version ("1.1.7")

//    alias(neoforged.plugins.common)
    alias(neoforged.plugins.moddev).apply(false)
}

subprojects {
    beforeEvaluate {
        plugins.apply(neoforged.plugins.moddev.get().pluginId)

        configure<NeoForgeExtension> {
            version = neoforged.versions.neoforge.get()
        }
    }
}