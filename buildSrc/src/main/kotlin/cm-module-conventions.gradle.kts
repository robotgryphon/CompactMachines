import java.text.SimpleDateFormat
import java.util.*

val vc = project.versionCatalogs
val neoforged = vc.named("neoforged")
val mojang = vc.named("mojang")

plugins {
    `java-library`
    `maven-publish`
}

afterEvaluate {
    plugins.apply {
        neoforged.findPlugin("moddev").orElseThrow()
    }
}

sourceSets {
    named("test") {
        resources {
            //The test module has no resources
            setSrcDirs(emptyList<String>())
        }
    }
}

java {
//    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withJavadocJar()
    withSourcesJar()
}


tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-proc:none")
    options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "9000"))
}

// Mark every module produced via this convention as an FML game library so its
// classes are loaded by NeoForge's TRANSFORMER classloader rather than the
// application classloader. Without this, classes that reference
// net.minecraft.server.MinecraftServer (or any other game class) crash with a
// LinkageError "loader constraint violation" the first time they are loaded.
tasks.withType<Jar>().configureEach {
    // `git rev-parse HEAD` writes "<sha>\n" — the trailing LF must be stripped
    // before the value is put into a Manifest attribute, otherwise the embedded
    // newline prematurely terminates the manifest's main section and the JAR
    // loader rejects everything that follows with
    //   java.io.IOException: invalid manifest format
    val gitVersion = providers.exec {
        commandLine("git", "rev-parse", "HEAD")
    }.standardOutput.asText.get().trimEnd()

    manifest {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())

        attributes(
            mapOf(
                "FMLModType" to "GAMELIBRARY",
                "Specification-Version" to "1", // We are version 1 of ourselves
                "Implementation-Timestamp" to now,
                "Implementation-Version" to version,
                "Minecraft-Version" to mojang.findVersion("minecraft").get(),
                "NeoForge-Version" to neoforged.findVersion("neoforge").get(),
                "Main-Commit" to gitVersion
            )
        )
    }
}