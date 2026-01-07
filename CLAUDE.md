# CLAUDE.md - Critical Orientation Minecraft Mod

## Project Overview

Critical Orientation is a multi-loader, multi-version Minecraft client-side mod. It allows players to snap their facing direction to the nearest cardinal or intercardinal point (N, NE, E, SE, S, SW, W, NW) with a keybind. This is primarily useful for precise ice boat placement.

## Supported Platforms

| Minecraft Version | Fabric | NeoForge | Forge |
|-------------------|--------|----------|-------|
| 1.21.4 | ✅ | ✅ | ❌ |
| 1.20.1 | ✅ | ❌ | ✅ |
| 1.19.4 | ✅ | ❌ | ✅ |
| 1.18.2 | ✅ | ❌ | ✅ |

## Tech Stack

- **Language**: Java 21
- **Build System**: Gradle 8.11.1 with Stonecraft + Stonecutter
- **Multi-Loader**: Stonecraft (combines Architectury + Stonecutter)
- **Testing**: JUnit 5

## Repository Structure

```
critical-orientation/
├── .github/workflows/
│   ├── build.yml                 # CI build for all loaders
│   └── release.yml               # Publish to CurseForge/Modrinth on tag
├── build.gradle.kts              # Stonecraft build configuration
├── settings.gradle.kts           # Stonecraft/Stonecutter setup
├── stonecutter.gradle.kts        # Active version configuration
├── gradle.properties             # Mod metadata
├── LICENSE                       # CC0-1.0 license
├── README.md                     # User-facing documentation
├── CLAUDE.md                     # AI assistant guidance (this file)
├── PLAN.md                       # Modernization roadmap
├── versions/                     # Generated version-specific builds
│   ├── 1.21.4-fabric/
│   ├── 1.21.4-neoforge/
│   ├── 1.20.1-fabric/
│   ├── 1.20.1-forge/
│   ├── 1.19.4-fabric/
│   ├── 1.19.4-forge/
│   ├── 1.18.2-fabric/
│   └── 1.18.2-forge/
└── src/
    ├── client/java/net/critical/orientation/
    │   ├── OrientationClient.java    # Loader-specific entry point
    │   └── OrientationKeyBind.java   # Keybinding (loader-aware)
    ├── main/java/net/critical/orientation/
    │   └── OrientationCommon.java    # Shared logic (all loaders)
    ├── main/resources/
    │   ├── fabric.mod.json           # Fabric metadata
    │   ├── META-INF/
    │   │   ├── mods.toml             # Forge metadata
    │   │   └── neoforge.mods.toml    # NeoForge metadata
    │   └── assets/orientation/lang/
    │       └── en_us.json            # English translations
    └── test/java/net/critical/orientation/
        └── OrientationKeyBindTest.java  # Unit tests
```

## Key Components

### OrientationCommon.java (Shared Logic)
- Contains loader-agnostic yaw calculation methods
- **`normalizeHeadYaw(double yaw)`**: Normalizes yaw to -180 to 180 range
- **`roundYaw(double yaw)`**: Rounds yaw to nearest 45-degree increment
- **`snapYaw(double yaw)`**: Combines normalize and round

### OrientationClient.java (Entry Point)
- Uses Stonecutter preprocessor directives for loader-specific code
- Fabric: Implements `ClientModInitializer`
- NeoForge/Forge: Uses `@Mod` annotation with event bus

### OrientationKeyBind.java (Keybinding)
- Registers backslash (`\`) key for direction snapping
- Loader-specific event registration via preprocessor

## Build Commands

```bash
# Build all versions and loaders
./gradlew chiseledBuild

# Build specific version
./gradlew :1.21.4-fabric:build

# Run tests
./gradlew test

# Run Minecraft client (active version)
./gradlew runClient

# Publish to CurseForge/Modrinth
./gradlew chiseledPublishMods
```

## Stonecutter Preprocessor

The project uses Stonecutter for conditional compilation:

```java
//? if fabric {
import net.fabricmc.api.ClientModInitializer;
//?} elif neoforge {
/*import net.neoforged.fml.common.Mod;
*///?}
```

- Active version set in `stonecutter.gradle.kts`
- Comments are uncommented/commented during build
- Supports version comparisons: `//? if >=1.20`

## Version Configuration

Managed in `gradle.properties`:
- `mod.id`: Mod identifier (`orientation`)
- `mod.name`: Display name
- `mod.version`: Current version (e.g., 2.0.0)
- `mod.group`: Package group (`net.critical`)

## CI/CD Pipeline

### Build Workflow
- Triggers on push to main/test/dev and PRs
- Runs `./gradlew chiseledBuild` for all loaders
- Uploads artifacts per loader type

### Release Workflow
- Triggers on version tags (e.g., `v2.0.0`)
- Runs `./gradlew chiseledPublishMods`
- Creates GitHub release with all JARs

### Required Secrets
- `CURSEFORGE_ID` / `CURSEFORGE_TOKEN`
- `MODRINTH_ID` / `MODRINTH_TOKEN`

## Code Conventions

- Package namespace: `net.critical.orientation`
- Mod ID: `orientation`
- Shared logic in `OrientationCommon.java`
- Use Stonecutter `//? if loader` for loader-specific code
- Tests use JUnit 5 with parameterized tests

## Distribution

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/critical-orientation)
- [Modrinth](https://modrinth.com/mod/critical-orientation)
- GitHub releases
- Licensed under CC0-1.0 (public domain)
