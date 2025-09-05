plugins {
    kotlin("jvm") version "2.0.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    kotlin("plugin.serialization") version "2.0.0"
}


val pluginVersion: String by project
group = "dev.rm20.mcfireworkshow"
version = "$pluginVersion-Snapshot"

val minecraftVersion: String by project
val slf4jVersion: String by project
val dotenvKotlinVersion: String by project
val fruxzAscendVersion: String by project
val fruxzStackedVersion: String by project
val kotlinxCoroutinesCoreVersion: String by project
val kotlinxCollectionsImmutableVersion: String by project
val gsonVersion: String by project
val mcCoroutineVersion: String by project

repositories {
    maven("https://nexus.modlabs.cc/repository/maven-mirrors/")
    maven(url = "https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven {
        url = uri("https://jitpack.io")
    }
    mavenCentral()
    flatDir {
        dirs("libs")
    }
    maven("https://libraries.minecraft.net/")
}

val deliverDependencies = listOf(
    "com.google.code.findbugs:jsr305:3.0.2",
    "com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:$mcCoroutineVersion",
    "com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:$mcCoroutineVersion",

    "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesCoreVersion",
    "org.jetbrains.kotlinx:kotlinx-collections-immutable:$kotlinxCollectionsImmutableVersion",
    "com.google.code.gson:gson:$gsonVersion",

    "dev.fruxz:ascend:$fruxzAscendVersion",
    "dev.fruxz:stacked:$fruxzStackedVersion",

    "io.github.cdimascio:dotenv-kotlin:$dotenvKotlinVersion", // - .env support
    "org.slf4j:slf4j-api:$slf4jVersion",
    "fr.skytasul:guardianbeam:2.4.4"
)

val includedDependencies = mutableListOf<String>()

fun Dependency?.deliver() = this?.apply {
    val computedVersion = version ?: kotlin.coreLibrariesVersion
    includedDependencies.add("${group}:${name}:${computedVersion}")
}

dependencies {
    paperweight.paperDevBundle("$minecraftVersion-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation(kotlin("stdlib")).deliver()
    implementation(kotlin("reflect")).deliver()
    deliverDependencies.forEach { dependency ->
        implementation(dependency).deliver()
    }
    implementation(platform("com.intellectualsites.bom:bom-newest:1.55"))
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

tasks.register("generateDependenciesFile") {
    group = "build"
    description = "Writes dependencies to file"

    val dependenciesFile = File(layout.buildDirectory.asFile.get(), "generated-resources/.dependencies")
    outputs.file(dependenciesFile)
    doLast {
        dependenciesFile.parentFile.mkdirs()
        dependenciesFile.writeText(includedDependencies.joinToString("\n"))
    }
}

tasks {
    build {
        paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
    }

    withType<ProcessResources> {
        dependsOn("generateDependenciesFile")

        from(File(layout.buildDirectory.asFile.get(), "generated-resources")) {
            include(".dependencies")
        }

        filesMatching("paper-plugin.yml") {
            expand(
                "version" to project.version,
                "name" to project.name,
            )
        }
    }

    register<JavaCompile>("compileMain") {
        source = fileTree("src/main/java")
        classpath = files(configurations.runtimeClasspath)
        destinationDirectory.set(file("build/classes/kotlin/main"))
        options.release.set(21)
    }
    jar {
        from(zipTree("libs/particlesfx-1.21.jar")) {
            exclude("META-INF/**")
        }
    }

}

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/kotlin")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        freeCompilerArgs.addAll(
            listOf(
                "-opt-in=kotlin.RequiresOptIn"
            )
        )
    }
}