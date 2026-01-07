# CLAUDE.md - Critical Orientation Minecraft Mod

## Project Overview

Critical Orientation is a Minecraft 1.17.x client-side mod built with the Fabric modding framework. It allows players to snap their facing direction to the nearest cardinal or intercardinal point (N, NE, E, SE, S, SW, W, NW) with a keybind. This is primarily useful for precise ice boat placement.

## Tech Stack

- **Language**: Java 16
- **Build System**: Gradle with Fabric Loom 0.9
- **Mod Framework**: Fabric (for Minecraft 1.17.1)
- **Dependencies**: Fabric API 0.39.2+1.17, Fabric Loader 0.11.6
- **Mappings**: Yarn 1.17.1+build.39

## Repository Structure

```
critical-orientation/
├── build.gradle              # Gradle build configuration with Fabric Loom
├── gradle.properties         # Version configuration for mod and dependencies
├── settings.gradle           # Gradle plugin repositories
├── gradlew / gradlew.bat     # Gradle wrapper scripts
├── LICENSE                   # CC0-1.0 license
├── README.md                 # User-facing documentation
└── src/main/
    ├── java/net/critical/orientation/
    │   ├── Orientation.java  # Main mod entry point (ModInitializer)
    │   └── MyKeyBind.java    # Keybinding and direction snapping logic
    └── resources/
        ├── fabric.mod.json   # Mod metadata and dependencies
        └── assets/orientation/lang/
            └── en_us.json    # English translations
```

## Key Components

### Orientation.java (Entry Point)
- Implements `ModInitializer` interface
- Registers the keybind handler on mod initialization

### MyKeyBind.java (Core Logic)
- Registers a keybind (default: backslash `\` key) in the "Critical Orientation" category
- Listens for key press events via `ClientTickCallback`
- **`normalizeHeadYaw(double yaw)`**: Normalizes yaw angle to -180 to 180 range
- **`roundYaw(double yaw)`**: Rounds yaw to nearest 45-degree increment (cardinal/intercardinal)
- Uses `refreshPositionAndAngles()` to update player facing direction

## Build Commands

```bash
# Build the mod JAR
./gradlew build

# Clean build artifacts
./gradlew clean

# Generate IDE configurations
./gradlew genSources          # Generate Minecraft sources for IDE
./gradlew eclipse             # For Eclipse
./gradlew idea                # For IntelliJ IDEA

# Run Minecraft client with mod
./gradlew runClient
```

Built JARs are output to `build/libs/critical-orientation-<version>.jar`

## Development Workflow

1. Clone the repository
2. Run `./gradlew genSources` to generate Minecraft source mappings
3. Import into IDE (IntelliJ IDEA recommended for Fabric mods)
4. Make changes to source files in `src/main/java/`
5. Test with `./gradlew runClient`
6. Build release JAR with `./gradlew build`

## Version Configuration

All versions are managed in `gradle.properties`:
- `mod_version`: Current mod version (e.g., 1.0.2)
- `minecraft_version`: Target Minecraft version
- `fabric_version`: Fabric API version
- `loader_version`: Fabric Loader version
- `yarn_mappings`: Yarn deobfuscation mappings

## Code Conventions

- Package namespace: `net.critical.orientation`
- Mod ID: `orientation`
- Use Fabric API event callbacks (e.g., `ClientTickCallback`)
- Keybinds use `FabricKeyBinding.Builder` pattern
- Debug output uses `System.out.println()` (remove for production)

## Important Notes

- This is a **client-side only** mod (no server component)
- Requires Fabric Loader >= 0.11.3 and Java >= 16
- The keybind category and registration uses deprecated Fabric API patterns (may need updating for newer Fabric versions)
- The `test()` method in MyKeyBind.java outputs debug info to console

## Distribution

- Available on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/critical-orientation)
- GitHub releases for direct downloads
- Licensed under CC0-1.0 (public domain)
