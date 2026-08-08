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

neoForge {
    // In-source siblings can't rely on moddev auto-propagating the published
    // interface-injection metadata across project deps, so read it explicitly.
    interfaceInjectionData {
        from(project(":api").file("interfaces.json"))
    }
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":room-system"))
    compileOnly(project(":shrinking"))
}
