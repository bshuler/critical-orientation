pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

plugins {
    id("gg.meza.stonecraft") version "1.9.+"
    id("dev.kikugie.stonecutter") version "0.8.+"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    shared {
        fun mc(version: String, vararg loaders: String) {
            for (loader in loaders) version("$version-$loader", version)
        }

        // ============================================
        // Minecraft 1.21.x - Fabric + NeoForge
        // ============================================
        // Using versions with confirmed Stonecraft/mod loader support
        mc("1.21.4", "fabric", "neoforge")   // Dec 2024 - The Garden Awakens
        mc("1.21.3", "fabric", "neoforge")   // Oct 2024
        mc("1.21.2", "fabric", "neoforge")   // Oct 2024
        mc("1.21.1", "fabric", "neoforge")   // Aug 2024 - Popular LTS
        mc("1.21", "fabric", "neoforge")     // Jun 2024 - Tricky Trials

        // ============================================
        // Minecraft 1.20.x
        // ============================================
        // 1.20.2+ uses NeoForge (after the Forge/NeoForge split July 2023)
        mc("1.20.6", "fabric", "neoforge")   // Apr 2024
        mc("1.20.4", "fabric", "neoforge")   // Dec 2023 - Popular stable
        mc("1.20.2", "fabric", "neoforge")   // Sep 2023

        // 1.20.1 and earlier use Forge (before NeoForge split)
        mc("1.20.1", "fabric", "forge")      // Jun 2023 - Most popular modded version
    }
    create(rootProject)
}

rootProject.name = "critical-orientation"
