plugins {
    id("java-library")
    id("eclipse")
    id("idea")
    id("maven-publish")
    id("net.neoforged.gradle.userdev") version ("7.0.57")
}

val mod_id: String by extra

val neoforge_version: String by extra
val coreVersion: String = property("core_version") as String

base {
    group = "dev.compactmods.compactmachines"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

sourceSets.main {
    resources {
        srcDir(file("src/main/resources"))
    }
}

repositories {
    mavenLocal()

    maven("https://maven.pkg.github.com/compactmods/compactmachines-core") {
        name = "Github PKG Core"
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("net.neoforged:neoforge:${neoforge_version}")

    compileOnly("dev.compactmods.compactmachines:core-api:$coreVersion")
    compileOnly("dev.compactmods.compactmachines:room-api:$coreVersion")
    compileOnly("dev.compactmods.compactmachines:room-upgrade-api:$coreVersion")
    compileOnly("dev.compactmods.compactmachines:core:$coreVersion")

    implementation(project(":neoforge-main"))
}

runs {
    // applies to all the run configs below
    configureEach {
        // Recommended logging data for a userdev environment
        systemProperty("forge.logging.markers", "") // 'SCAN,REGISTRIES,REGISTRYDUMP'

        // Recommended logging level for the console
        systemProperty("forge.logging.console.level", "debug")

        // ideaModule("Compact_Crafting.forge-main.main")
        modSource(project.sourceSets.main.get())
    }

    create("data") {
        workingDirectory(file("run/data"))

        programArguments("--mod", "compactmachines")
        programArguments("--all")
        programArguments("--output", project(":neoforge-main").file("src/generated/resources").path)
        programArguments("--existing", project(":neoforge-main").file("src/main/resources").path)
    }
}

tasks.compileJava {
    options.encoding = "UTF-8";
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}