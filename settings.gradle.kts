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
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("gg.meza.stonecraft") version "1.12.+"
    id("dev.kikugie.stonecutter") version "0.9.+"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    shared {
        fun mc(version: String, vararg loaders: String) {
            for (loader in loaders) {
                version("$version-$loader", version)
            }
        }

        // Minecraft renamed its versioning scheme after 1.21.11 (see
        // https://fabricmc.net/2026/03/14/261.html); 26.1+ ships unobfuscated
        // with Mojang's official names and Yarn/Intermediary are no longer
        // published for it. Fabric + NeoForge only from here on - Forge ended
        // at the NeoForge split (1.20.2).

        // Current (unobfuscated, official mappings)
        mc("26.2", "fabric", "neoforge")
        mc("26.1.2", "fabric", "neoforge")
        mc("26.1.1", "fabric", "neoforge")
        mc("26.1", "fabric", "neoforge")

        // 1.21.x (Yarn-mapped) - Fabric + NeoForge
        mc("1.21.11", "fabric", "neoforge")
        mc("1.21.10", "fabric", "neoforge")
        mc("1.21.9", "fabric", "neoforge")
        mc("1.21.8", "fabric", "neoforge")
        mc("1.21.7", "fabric", "neoforge")
        mc("1.21.6", "fabric", "neoforge")
        mc("1.21.5", "fabric", "neoforge")
        mc("1.21.4", "fabric", "neoforge")

        // 1.20.1 - Fabric + Forge (last major Forge version before NeoForge split)
        mc("1.20.1", "fabric", "forge")
        // 1.19.4 - Fabric + Forge
        mc("1.19.4", "fabric", "forge")
        // 1.18.2 - Fabric + Forge
        mc("1.18.2", "fabric", "forge")

        vcsVersion = "26.2-fabric"
    }
    create(rootProject)
}

rootProject.name = "critical-orientation"
