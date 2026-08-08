val versionMain: String = System.getenv("ROOM_UPGRADES_VERSION") ?: "9.9.9"

plugins {
    id("cm-module-conventions")
}

base {
    archivesName = "room-upgrades"
    group = "dev.compactmods.compactmachines"
    version = versionMain
}

// See room-system/build.gradle.kts for the rationale; identical fix applies
// here so the JIJ-extracted copy and the direct project-dep copy share an
// auto-module name and JarSelector can dedupe them.
tasks.withType<Jar>().configureEach {
    manifest {
        attributes(mapOf("Automatic-Module-Name" to "compactmachines.room.upgrades"))
    }
}

val storageSource = sourceSets.register("storage")

neoForge.addModdingDependenciesTo(storageSource.get())

sourceSets.main {
    java {
        srcDir(storageSource.get().java)
    }
}

repositories {
    maven("https://maven.pkg.github.com/compactmods/feather") {
        name = "Github PKG - Feather"
        content {
            includeGroup("dev.compactmods")
            includeModule("dev.compactmods", "feather")
        }

        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":room-system"))
    compileOnly(compactmods.spatial)

    "storageCompileOnly"(project(":api"))
    "storageCompileOnly"(project(":room-system"))
}
