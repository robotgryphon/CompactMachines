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

val mainProject: Project = project(":neoforge-main")
evaluationDependsOn(mainProject.path)

base {
    group = "dev.compactmods.compactmachines"
    archivesName.set(mod_id)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

minecraft {
    modIdentifier.set(mod_id)
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
        modSource(mainProject.sourceSets.main.get())
    }

    create("data") {
        dataGenerator(true)
        workingDirectory(file("run/data"))

        programArguments("--mod", "compactmachines")
        programArguments("--all")
        programArguments("--output", project(":neoforge-main").file("src/generated/resources").absolutePath)
        programArguments("--existing", project(":neoforge-main").file("src/main/resources").absolutePath)
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

    implementation("dev.compactmods.compactmachines:core-api:$coreVersion")
    implementation("dev.compactmods.compactmachines:room-api:$coreVersion")
    implementation("dev.compactmods.compactmachines:room-upgrade-api:$coreVersion")
    implementation("dev.compactmods.compactmachines:core:$coreVersion")

    implementation(project(":neoforge-main")) {
        isTransitive = false
    }
}

tasks.compileJava {
    options.encoding = "UTF-8";
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}