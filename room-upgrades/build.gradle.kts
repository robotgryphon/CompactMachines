val versionMain: String = System.getenv("ROOM_UPGRADES_VERSION") ?: "9.9.9"

plugins {
    id("cm-module-conventions")
}

base {
    archivesName = "room-upgrades"
    group = "dev.compactmods.compactmachines"
    version = versionMain
}

val apiSource = sourceSets.register("api")

neoForge.addModdingDependenciesTo(apiSource.get())

sourceSets.main {
    java {
        srcDir(apiSource.get().java)
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
    compileOnly(project(":core"))
    compileOnly(project(":room-system"))
    compileOnly(project(":dimension-api"))

    "apiCompileOnly"(project(":core"))
    "apiCompileOnly"(project(":room-system"))
}