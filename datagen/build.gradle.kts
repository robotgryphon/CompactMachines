plugins {
    id("java")
    id("eclipse")
    id("idea")
    id("maven-publish")
    alias(neoforged.plugins.moddev)
}

val modId: String = "compactmachines"

val coreApi = project(":core")
val mainProject: Project = project(":compactmachines")

project.evaluationDependsOn(coreApi.path)
project.evaluationDependsOn(mainProject.path)

java {
//    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

neoForge {
    mods.create(modId) {
        this.sourceSet(sourceSets.main.get())
        this.sourceSet(mainProject.sourceSets.main.get())
//        this.sourceSet(coreApi.sourceSets.main.get())
    }

    runs {
        this.register("data") {
            this.clientData()

            this.gameDirectory.set(file("runs/data"))

            programArguments.addAll("--mod", modId)
            programArguments.addAll("--all")
            programArguments.addAll("--output", mainProject.file("src/generated/resources").absolutePath)
            programArguments.addAll("--existing", mainProject.file("src/main/resources").absolutePath)
        }
    }
}

repositories {
    mavenLocal()

    maven("https://maven.pkg.github.com/compactmods/feather") {
        name = "Github PKG Core"
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }

    maven("https://maven.theillusivec4.top/") {
        name = "Illusive Soulworks maven (Curios API)"
        content {
            includeGroup("top.theillusivec4.curios")
        }
    }
}

dependencies {
    compileOnly(coreApi)
    implementation(mainProject)
    compileOnly(project(":compactmachines-api"))  // dev.compactmods.machines.api.CompactMachines
    compileOnly(project(":dimension-api"))         // dev.compactmods.machines.api.dimension.CompactDimension
    compileOnly(project(":room-system"))           // dev.compactmods.machines.api.room.template.RoomTemplate (api source set)
    compileOnly(project(":shrinking"))
//    implementation(libs.curios)
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}