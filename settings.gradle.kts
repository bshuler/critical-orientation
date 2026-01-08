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
        // (uses yarn-mappings-patch-neoforge 1.21+build.x)
        // ============================================
        mc("1.21.11", "fabric", "neoforge")  // Latest (Dec 2025)
        mc("1.21.4", "fabric", "neoforge")   // Dec 2024 - The Garden Awakens
        mc("1.21.1", "fabric", "neoforge")   // Aug 2024 - Popular LTS
        mc("1.21", "fabric", "neoforge")     // Jun 2024 - Tricky Trials

        // ============================================
        // Minecraft 1.20.5-1.20.6 - Fabric + NeoForge
        // (uses yarn-mappings-patch-neoforge 1.20.5/1.20.6+build.x)
        // ============================================
        mc("1.20.6", "fabric", "neoforge")   // Apr 2024
        mc("1.20.5", "fabric", "neoforge")   // Apr 2024

        // ============================================
        // Minecraft 1.20.3-1.20.4 - Fabric ONLY
        // (Forge 1.20.3+ has missing dependencies like bootstrap-dev:2.0.0)
        // (NeoForge mapping patches not available for these versions)
        // ============================================
        mc("1.20.4", "fabric")               // Dec 2023
        mc("1.20.3", "fabric")               // Dec 2023

        // ============================================
        // Minecraft 1.20-1.20.1 - Fabric + Forge
        // (pre-NeoForge split, no patches needed)
        // ============================================
        mc("1.20.1", "fabric", "forge")      // Jun 2023 - Most popular modded version
        mc("1.20", "fabric", "forge")        // Jun 2023 - Trails & Tales

        // ============================================
        // Legacy versions - Fabric + Forge
        // ============================================
        mc("1.19.4", "fabric", "forge")
        mc("1.18.2", "fabric", "forge")
    }
    create(rootProject)
}

rootProject.name = "critical-orientation"
