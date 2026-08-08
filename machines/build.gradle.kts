val versionMain: String = System.getenv("MACHINES_VERSION") ?: "9.9.9"

plugins {
    id("cm-module-conventions")
}

base {
    archivesName = "machines"
    group = "dev.compactmods.compactmachines"
    version = versionMain
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(mapOf("Automatic-Module-Name" to "compactmachines.machines"))
    }
}

// The `api` source set holds the public machine surface (block-entity
// interfaces, machine constants) — mirrors :room-system's api/main split.
// It is compiled in isolation and folded back into `main` via srcDir so the
// whole module still ships as a single jar.
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
    compileOnly(project(":room-system"))
    compileOnly(project(":shrinking"))

    // The `api` source set gets its own parallel configuration chain
    // (apiCompileOnly / …). Anything imported from src/api/java must be listed
    // here explicitly — the plain compileOnly(...) above only feeds `main`.
    // See room-system/build.gradle.kts for the full rationale.
    "apiCompileOnly"(project(":core"))
}
