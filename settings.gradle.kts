pluginManagement {
    repositories {
        gradlePluginPortal()
        // NeoForged: ModDevGradle plugin + NeoForge artifacts
        maven("https://maven.neoforged.net/releases")
        // Parchment mappings
        maven("https://maven.parchmentmc.org")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "Mantle"
