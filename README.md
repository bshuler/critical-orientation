# Critical Orientation

[![Build Status](https://img.shields.io/github/actions/workflow/status/bshuler/critical-orientation/build.yml?branch=main&style=flat-square&logo=github&label=Build)](https://github.com/bshuler/critical-orientation/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-CC0_1.0-blue?style=flat-square)](LICENSE)
[![GitHub Release](https://img.shields.io/github/v/release/bshuler/critical-orientation?style=flat-square&logo=github&label=Release)](https://github.com/bshuler/critical-orientation/releases/latest)

### Supported Loaders
[![Fabric](https://img.shields.io/badge/Fabric-1.18.2--26.2-dbd0b4?style=flat-square&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAAsTAAALEwEAmpwYAAABnUlEQVR4nO2Wv0oDQRDGf4mFWFgIFhYWgoWFhYWFhYWFhYWFhYWFhYWFYGFhYWEhWFhYWFgIFhb+K+yysLnsXe4uOUH9YGBv5+a+b3Zndg+K+MeiKkMn4BZYBw6AY+C5k0xCTALzwBLwBKwBe8BtHukdYAGYAQ6B7SQIvQcucnbsYwvYTwGAWWAXuAH2kwDgA7gCLjIBkB/5NgkAfuTbJAD4kW+TACC/gCQAMCLfJgEA+QUkAYD8ApIAwI98mwQA5BeQBADyC0gCAPkFJAGA/AKSAEDOeQHYAzaAo6JQXgBmgSVgFzgE9oHbTICYAuYMvwK7wC5wCBwAx8ApMJkCALPADbAJHAJ7wHEKAMwA28A6sAccASfAWQoAjBn8EtgBDoATI6ckAPwU8CPfJgEA+QUkAQD5BSQBAPkFJAEA+QUkAQD5BSQBgJzzArAHbBiBilB+AKaBZWAP2AWOgNMUABgz+CWwAxwAJ0ZOSQAgP/JtEgCQX0ASAJBfQBIAkF9AEgCQX0ASAJBfQBIAkF9AEgCQH/kfin+JH8bBKCoJ2N+IAAAAAElFTkSuQmCC)](https://fabricmc.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-1.21.4--26.2-f38821?style=flat-square&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAAsTAAALEwEAmpwYAAABaElEQVR4nO2WMU7DQBBF30IKGgoKCgoKCgoKCgoKCgoKCgoKCgqgoKCgoACJgoKCgkIi/+0KybuzXq8dR+I/6cjy7sy8nfUEtvjPIgswBuyEv0dxBeAe2AZGcSbnAIvAEvAAbAPDNJiUAMwDS8A9sAXcA6c0BDAKrAI3wA4wBDaSAOBLvpUEAN/yrSQA+JZvJQHAt3wrCQC+5VtJAPAt30oCgG/5VhIAfMu3kgDgW76VBADf8q0kAPiWbyUBwLd8KwkAvuVbSQDwLd9KAoBv+VYSAHzLt5IA4Fu+lQQA3/KtJAD4lm8lAcC3fCsJAL7lW0kA8C3fSgKAb/lWEgB8y7eSAOBbvpUEAN/yrSQA+JZvJQHAt3wrCQC+5VtJAPAt30oCgG/5VhIAfMu3kgDgW76VBADf8q0kAPiWbyUBwLd8KwkAvuVbSQDwLd9KAoBv+VYSAHzLt5IA8F/lR/4Px0+WH+4XG10AAAAASUVORK5CYII=)](https://neoforged.net/)
[![Forge](https://img.shields.io/badge/Forge-1.18.2--1.20.1-1f72b7?style=flat-square&logo=curseforge&logoColor=white)](https://files.minecraftforge.net/)

### Minecraft Versions
30 Fabric/Forge/NeoForge builds spanning **1.18.2 through 26.2** (the newest
stable Minecraft release at time of writing). Full per-version, per-loader
matrix is in [`PLAN.md`](PLAN.md).

### Download From
[![CurseForge](https://img.shields.io/badge/CurseForge-Download-f16436?style=flat-square&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/critical-orientation)
[![Modrinth](https://img.shields.io/badge/Modrinth-Download-00af5c?style=flat-square&logo=modrinth)](https://modrinth.com/mod/critical-orientation)
[![GitHub](https://img.shields.io/badge/GitHub-Releases-181717?style=flat-square&logo=github)](https://github.com/bshuler/critical-orientation/releases)

---

A Minecraft client-side mod that snaps your facing direction to the nearest cardinal or intercardinal point (N, NE, E, SE, S, SW, W, NW) with a keybind. Perfect for precise ice boat placement!

## Supported Versions

| Range | Fabric | NeoForge | Forge |
|:-----:|:------:|:--------:|:-----:|
| 1.21.4 - 26.2 | ✅ | ✅ | — |
| 1.18.2 - 1.20.1 | ✅ | — | ✅ |

Full per-version matrix (15 Minecraft versions, 30 builds total) is in [`PLAN.md`](PLAN.md).
Quilt is not built directly but is expected to work via its Fabric API compatibility layer.

## Features

- **Direction Snapping**: Press the keybind (default: `\` backslash) to instantly snap to the nearest cardinal or intercardinal direction
- **8 Directions**: North, Northeast, East, Southeast, South, Southwest, West, Northwest
- **Client-Side Only**: No server installation required
- **Multi-Loader**: Available for Fabric, NeoForge, and Forge

## Installation

### Fabric
1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download the Fabric version of Critical Orientation
4. Place the `.jar` file in your `mods` folder

### NeoForge (1.21.4 - 26.2)
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
4. Customize the keybind in **Options > Controls > Critical Orientation**

## Building from Source

```bash
# Clone the repository
git clone https://github.com/bshuler/critical-orientation.git
cd critical-orientation

# Build all versions and loaders
./gradlew chiseledBuild

# Built JARs will be in versions/*/build/libs/
```

## Technical Details

| Component | Technology |
|-----------|------------|
| Language | Java (17-25, toolchain-selected per Minecraft version) |
| Build System | Gradle 9.7.0 + Stonecraft |
| Multi-Version | Stonecutter |
| Multi-Loader | Architectury Loom (via Stonecraft) |
| Testing | JUnit 5 |

## License

[![CC0](https://img.shields.io/badge/License-CC0_1.0-blue?style=flat-square)](LICENSE)

This project is licensed under [CC0-1.0](LICENSE) (Public Domain). You are free to use, modify, and distribute this code without restriction.

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

---

Made with ❤️ for the Minecraft ice boat racing community
