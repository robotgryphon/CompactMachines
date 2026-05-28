import java.text.SimpleDateFormat
import java.util.*

val versionMain: String = System.getenv("DIMENSION_API_VERSION") ?: "9.9.9"

plugins {
    id("java-library")
    id("maven-publish")
    alias(neoforged.plugins.moddev)
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
    archivesName = "dimension-api"
    group = "dev.compactmods.compactmachines"
    version = versionMain
}

java {
//    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation(project(":core"))
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
                "Automatic-Module-Name" to "compactmachines.api.dimension",
                "Specification-Title" to "Compact Machines - Dimension API",
                "Implementation-Title" to "Compact Machines - Dimension API"
            )
        )
    }
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactmachines"
publishing {
    publications.register<MavenPublication>("dimension-api") {
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