val versionMain: String = System.getenv("SHRINKING_API_VERSION") ?: "9.9.9"

plugins {
    id("cm-module-conventions")
}

base {
    archivesName = "shrinking-api"
    group = "dev.compactmods.compactmachines"
    version = versionMain
}

neoForge.interfaceInjectionData.from(project(":api").file("interfaces.json"))

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
    compileOnly(compactmods.feather)
    compileOnly(project(":api"))
    compileOnly(project(":room-system"))
}

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Automatic-Module-Name" to "compactmachines.api.shrinking",
                "Specification-Title" to "Compact Machines - Shrinking API"
            )
        )
    }
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactmachines"
publishing {
    publications.register<MavenPublication>("shrinking-api") {
        from(components.getByName("java"))
    }

    repositories {
        // GitHub Packages
        maven(PACKAGES_URL) {
            name = "GitHubPackages"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
