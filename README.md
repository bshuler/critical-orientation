# Critical Orientation

A Minecraft client-side mod that snaps your facing direction to the nearest cardinal or intercardinal point (N, NE, E, SE, S, SW, W, NW) with a keybind. Perfect for precise ice boat placement!

## Downloads

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/critical-orientation)
- [Modrinth](https://modrinth.com/mod/critical-orientation)
- [GitHub Releases](https://github.com/bshuler/critical-orientation/releases)

## Supported Versions

| Minecraft | Fabric | NeoForge | Forge |
|-----------|--------|----------|-------|
| 1.21.4    | ✅     | ✅       | ❌    |
| 1.20.1    | ✅     | ❌       | ✅    |
| 1.19.4    | ✅     | ❌       | ✅    |
| 1.18.2    | ✅     | ❌       | ✅    |

## Features

- **Direction Snapping**: Press the keybind (default: `\` backslash) to instantly snap to the nearest cardinal or intercardinal direction
- **8 Directions**: North, Northeast, East, Southeast, South, Southwest, West, Northwest
- **Client-Side Only**: No server installation required

## Installation

### Fabric
1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download the Fabric version of Critical Orientation
4. Place the `.jar` file in your `mods` folder

### NeoForge (1.21.4+)
1. Install [NeoForge](https://neoforged.net/)
2. Download the NeoForge version of Critical Orientation
3. Place the `.jar` file in your `mods` folder

### Forge (1.20.1 and earlier)
1. Install [Forge](https://files.minecraftforge.net/)
2. Download the Forge version of Critical Orientation
3. Place the `.jar` file in your `mods` folder

## Usage

1. Launch Minecraft with the mod installed
2. Enter a world
3. Press the `\` (backslash) key to snap to the nearest direction
4. Customize the keybind in Options > Controls > Critical Orientation

## Building from Source

```bash
# Clone the repository
git clone https://github.com/bshuler/critical-orientation.git
cd critical-orientation

# Build all versions and loaders
./gradlew chiseledBuild

# Built JARs will be in versions/*/build/libs/
```

## License

This project is licensed under [CC0-1.0](LICENSE) (Public Domain).

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.
