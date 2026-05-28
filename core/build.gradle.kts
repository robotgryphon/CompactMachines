val versionMain: String = System.getenv("CORE_VERSION") ?: "9.9.9"

plugins {
    id("java-library")
    id("maven-publish")
    id("cm-module-conventions")
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

//neoForge{
//    interfaceInjectionData {
//        this.from(project.file("interfaces.json"))
//        this.publish(project.file("interfaces.json"))
//    }
//}

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