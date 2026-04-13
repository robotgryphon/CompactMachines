import org.gradle.kotlin.dsl.the

val vc = project.versionCatalogs
val neoforged = vc.named("neoforged");

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