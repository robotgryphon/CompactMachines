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

val apiSource = sourceSets.register("api")

neoForge {
    addModdingDependenciesTo(apiSource.get())
    interfaceInjectionData {
        from(project(":core").file("interfaces.json"))
    }
}

sourceSets.main {
    java {
        srcDir(apiSource.get().java)
        srcDir("src/main/java")
    }
}

dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":dimension-api"))

    // The `api` source set is compiled in isolation by Gradle and gets its own
    // parallel configuration chain (apiCompileOnly / apiImplementation / …).
    // Every project consumed from a source file under src/api/java must be
    // listed here explicitly — `compileOnly(...)` above only feeds the `main`
    // source set. The IDE flattens source-set classpaths so unqualified
    // imports look fine in the editor but fail in `./gradlew compileApiJava`.
    "apiCompileOnly"(project(":core"))
    "apiCompileOnly"(project(":dimension-api"))

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