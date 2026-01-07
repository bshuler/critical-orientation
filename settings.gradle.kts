pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

plugins {
    id("gg.meza.stonecraft") version "1.9.+"
    id("dev.kikugie.stonecutter") version "0.5"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    fun mc(version: String, vararg loaders: String) {
        for (loader in loaders) {
            create("$version-$loader", version, loader)
        }
    }

    // Target versions and loaders
    mc("1.21.4", "fabric", "neoforge")
    mc("1.20.1", "fabric", "forge")

    vcsVersion = "1.21.4-fabric"
}

rootProject.name = "critical-orientation"
