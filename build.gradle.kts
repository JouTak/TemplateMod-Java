val group: String by project
val version: String by project
val repo: String by project
val mod_name: String by project
val mod_id: String by project

project.group = group
project.version = version

plugins {
    alias(libs.plugins.fabric.loom)
    java
}

val javaVersion = libs.versions.jdk.get().toInt()

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
}

dependencies {
    minecraft("com.mojang:minecraft:${libs.versions.minecraft.get()}")
    implementation("net.fabricmc:fabric-loader:${libs.versions.fabric.loader.get()}")
    implementation(libs.fabric.api)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaVersion)
}

tasks.processResources {
    val commitHash = project.findProperty("commitHash") as String?

    val website =
        if (repo.isBlank()) {
            "https://joutak.ru"
        } else {
            if (commitHash.isNullOrBlank()) repo else "$repo/tree/$commitHash"
        }

    val props =
        mapOf(
            "MOD_NAME" to mod_name,
            "MOD_ID" to mod_id,
            "VERSION" to project.version,
            "MINECRAFT_VERSION" to libs.versions.minecraft.get(),
            "WEBSITE" to website,
        )

    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}

tasks.jar {
    archiveFileName.set("${project.name}-${project.version}.jar")

    if (System.getenv("TEST_MOD_BUILD") != null) {
        val modsPath = System.getenv("MODS_PATH")
        if (modsPath != null) {
            destinationDirectory.set(file(modsPath))
        } else {
            logger.warn("MODS_PATH property is not set!")
        }
    }
}
