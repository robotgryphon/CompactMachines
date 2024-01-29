pluginManagement {
    plugins {
        id("idea")
        id("eclipse")
        id("maven-publish")
        id("java-library")
    }

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()

        // maven("https://maven.architectury.dev/")

        maven("https://maven.parchmentmc.org") {
            name = "ParchmentMC"
        }

        maven("https://maven.neoforged.net/releases") {
            name = "NeoForged"
        }

        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "Sponge Snapshots"
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.5.0")
}

include(":core:core")
include(":core:core-api")
include(":core:room-api")
include(":core:room-upgrade-api")

project(":core:core").projectDir = file("./core/core")
project(":core:core-api").projectDir = file("./core/core-api")
project(":core:room-api").projectDir = file("./core/room-api")
project(":core:room-upgrade-api").projectDir = file("./core/room-upgrade-api")

include("neoforge-main")
// include("neoforge-datagen")
// includeBuild("core")

