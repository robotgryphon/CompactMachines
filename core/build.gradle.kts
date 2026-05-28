val versionMain: String = System.getenv("CORE_VERSION") ?: "9.9.9"

plugins {
    id("java-library")
    id("maven-publish")
    id("cm-module-conventions")
    // moddev is applied here (rather than only via cm-module-conventions'
    // afterEvaluate hook) so the `neoForge { interfaceInjectionData {…} }`
    // extension below is available at script-evaluation time.
    alias(neoforged.plugins.moddev)
}

sourceSets {
    named("test") {
        resources {
            //The test module has no resources
            setSrcDirs(emptyList<String>())
        }
    }
}

base {
    archivesName = "core"
    group = "dev.compactmods.compactmachines"
    version = versionMain
}

java {
//    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withJavadocJar()
    withSourcesJar()
}

// Interface injection: at compile time, moddev rewrites the bytecode of every
// type listed in interfaces.json so it appears to implement the matching
// interface. Configured here in :core so that the metadata travels with the
// core artifact — every downstream module that depends on :core
// (compactmachines, room-system, room-upgrades, shrinking, …) automatically
// sees `MinecraftServer implements IForwardingAttachmentHolder` and can call
// `server.getData(…)` without a cast. The matching runtime addition comes
// from MinecraftServerMixin in this same module.
//
// `from(…)` consumes the JSON during *this* module's compile/runtime; `publish(…)`
// attaches it to the Maven publication so consumers' moddev plugins resolve and
// apply the same injection transitively.
neoForge {
    interfaceInjectionData {
        from(project.file("interfaces.json"))
        publish(project.file("interfaces.json"))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-proc:none")
    options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "9000"))
}

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Automatic-Module-Name" to "compactmachines.core",
                "Specification-Title" to "Compact Machines - Core",
                "Implementation-Title" to "Compact Machines - Core"
            )
        )
    }
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactmachines"
publishing {
    publications.register<MavenPublication>("core") {
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