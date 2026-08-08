val versionMain: String = System.getenv("ROOM_SYSTEM_VERSION") ?: "9.9.9"

plugins {
    id("cm-module-conventions")
}

base {
    archivesName = "rooms"
    group = "dev.compactmods.compactmachines"
    version = versionMain
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(mapOf("Automatic-Module-Name" to "compactmachines.rooms"))
    }
}

neoForge {
    // In-source siblings can't rely on moddev auto-propagating the published
    // interface-injection metadata across project deps, so read it explicitly.
    interfaceInjectionData {
        from(project(":api").file("interfaces.json"))
    }
}

dependencies {
    compileOnly(project(":api"))

    implementation(libs.jnanoid)
    implementation(compactmods.feather)
    implementation(compactmods.spatial)
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
