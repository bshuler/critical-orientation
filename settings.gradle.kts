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
    id("dev.kikugie.stonecutter") version "0.8.+"
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
    // Latest (1.21.4) - Fabric + NeoForge
    mc("1.21.4", "fabric", "neoforge")
    // 1.20.1 - Fabric + Forge (last major Forge version before NeoForge split)
    mc("1.20.1", "fabric", "forge")
    // 1.19.4 - Fabric + Forge
    mc("1.19.4", "fabric", "forge")
    // 1.18.2 - Fabric + Forge
    mc("1.18.2", "fabric", "forge")
}

rootProject.name = "critical-orientation"
