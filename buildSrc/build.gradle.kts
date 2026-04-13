plugins {
    `kotlin-dsl`
    alias(neoforged.plugins.moddev)
}

repositories { gradlePluginPortal() }

neoForge {
    version = neoforged.versions.neoforge.get()
}