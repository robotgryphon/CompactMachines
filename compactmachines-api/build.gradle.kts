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