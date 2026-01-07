# CLAUDE.md - Critical Orientation Minecraft Mod

## Project Overview

Critical Orientation is a Minecraft 1.21.4 client-side mod built with the Fabric modding framework. It allows players to snap their facing direction to the nearest cardinal or intercardinal point (N, NE, E, SE, S, SW, W, NW) with a keybind. This is primarily useful for precise ice boat placement.

## Tech Stack

- **Language**: Java 21
- **Build System**: Gradle 8.11.1 with Fabric Loom 1.9
- **Mod Framework**: Fabric (for Minecraft 1.21.4)
- **Dependencies**: Fabric API 0.119.2+1.21.4, Fabric Loader 0.16.10
- **Mappings**: Yarn 1.21.4+build.8
- **Testing**: JUnit 5

## Repository Structure

```
critical-orientation/
├── .github/workflows/
│   ├── build.yml             # CI build on push/PR
│   └── release.yml           # Publish to CurseForge/Modrinth on tag
├── build.gradle              # Gradle build configuration with Fabric Loom
├── gradle.properties         # Version configuration for mod and dependencies
├── settings.gradle           # Gradle plugin repositories
├── gradlew / gradlew.bat     # Gradle wrapper scripts
├── LICENSE                   # CC0-1.0 license
├── README.md                 # User-facing documentation
├── CLAUDE.md                 # AI assistant guidance (this file)
├── PLAN.md                   # Modernization roadmap
└── src/
    ├── client/java/net/critical/orientation/
    │   ├── OrientationClient.java    # Client mod entry point
    │   └── OrientationKeyBind.java   # Keybinding and direction snapping logic
    ├── main/resources/
    │   ├── fabric.mod.json           # Mod metadata and dependencies
    │   └── assets/orientation/lang/
    │       └── en_us.json            # English translations
    └── test/java/net/critical/orientation/
        └── OrientationKeyBindTest.java  # Unit tests for yaw calculations
```

## Key Components

### OrientationClient.java (Entry Point)
- Implements `ClientModInitializer` interface
- Registers the keybind handler on client initialization

### OrientationKeyBind.java (Core Logic)
- Registers a keybind (default: backslash `\` key) in the "Critical Orientation" category
- Listens for key press events via `ClientTickEvents.END_CLIENT_TICK`
- **`normalizeHeadYaw(double yaw)`**: Normalizes yaw angle to -180 to 180 range
- **`roundYaw(double yaw)`**: Rounds yaw to nearest 45-degree increment (cardinal/intercardinal)
- Updates player yaw, head yaw, and body yaw on key press

## Build Commands

```bash
# Build the mod JAR
./gradlew build

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean

# Generate IDE configurations
./gradlew genSources          # Generate Minecraft sources for IDE

# Run Minecraft client with mod
./gradlew runClient
```

Built JARs are output to `build/libs/critical-orientation-<version>.jar`

## Development Workflow

1. Clone the repository
2. Run `./gradlew genSources` to generate Minecraft source mappings
3. Import into IDE (IntelliJ IDEA recommended for Fabric mods)
4. Make changes to source files in `src/client/java/`
5. Run tests with `./gradlew test`
6. Test in-game with `./gradlew runClient`
7. Build release JAR with `./gradlew build`

## Version Configuration

All versions are managed in `gradle.properties`:
- `mod_version`: Current mod version (e.g., 2.0.0)
- `minecraft_version`: Target Minecraft version
- `fabric_version`: Fabric API version
- `loader_version`: Fabric Loader version
- `yarn_mappings`: Yarn deobfuscation mappings

## Code Conventions

- Package namespace: `net.critical.orientation`
- Mod ID: `orientation`
- Use Fabric API event callbacks (`ClientTickEvents.END_CLIENT_TICK`)
- Keybinds use `KeyBindingHelper.registerKeyBinding()` pattern
- Client-side code in `src/client/java/` (uses split source sets)
- Tests in `src/test/java/` using JUnit 5

## CI/CD Pipeline

### Build Workflow (`.github/workflows/build.yml`)
- Triggers on push to main/test/dev branches and PRs
- Runs Gradle build and tests
- Uploads build artifacts

### Release Workflow (`.github/workflows/release.yml`)
- Triggers on version tags (e.g., `v2.0.0`)
- Publishes to CurseForge and Modrinth
- Creates GitHub release

### Required Secrets
- `CURSEFORGE_ID` / `CURSEFORGE_TOKEN`
- `MODRINTH_ID` / `MODRINTH_TOKEN`

## Important Notes

- This is a **client-side only** mod (no server component)
- Requires Fabric Loader >= 0.16.0 and Java >= 21
- Uses modern Fabric API patterns (KeyBindingHelper, ClientTickEvents)

## Distribution

- Available on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/critical-orientation)
- GitHub releases for direct downloads
- Licensed under CC0-1.0 (public domain)
