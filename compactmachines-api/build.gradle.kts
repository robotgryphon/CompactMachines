val versionMain: String = System.getenv("API_VERSION") ?: "9.9.9"

plugins {
    base
    `java-library`
    `maven-publish`
}

base {
    archivesName = "api"
    group = "dev.compactmods.compactmachines"
    version = versionMain
}

repositories {
    mavenLocal()
    maven("https://maven.pkg.github.com/compactmods/spatial") {
        name = "Github PKG - Spatial"
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":room-system"))
    compileOnly(project(":room-upgrades"))
}

// :compactmachines-api is the public-API facade other mods compile against,
// but it also has to load at runtime so that internal callers (e.g. :datagen)
// can use CompactMachines.identifier / MOD_ID without ClassNotFoundException.
//
// • FMLModType=GAMELIBRARY puts the jar on NeoForge's TRANSFORMER classloader
//   instead of the app classloader. Without this, the first reference to
//   net.minecraft.server.MinecraftServer in any class loaded from this jar
//   trips a LinkageError loader-constraint violation (same bug we hit with
//   room-upgrades earlier in the branch).
// • Automatic-Module-Name pins a stable JPMS module identity so NeoForge's
//   JarSelector can dedupe the JIJ-extracted copy against the direct project
//   dep, the way it already does for compactmachines.core / .api.dimension /
//   .api.shrinking / .rooms / .room.upgrades.
tasks.withType<Jar>().configureEach {
    manifest {
        attributes(mapOf(
            "FMLModType" to "GAMELIBRARY",
            "Automatic-Module-Name" to "compactmachines.api"
        ))
    }
}