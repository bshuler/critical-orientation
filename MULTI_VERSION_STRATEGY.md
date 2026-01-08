# Minecraft Java Mod Multi-Loader Compilation Prompt

You are tasked with compiling a Minecraft Java Edition mod for **all possible mod loaders** and **all Minecraft versions they support**. Below is the complete reference of mod loaders, their supported versions, and compatibility information.

---

## MOD LOADERS REFERENCE

### 1. NeoForge
**Description:** Fork of Minecraft Forge (July 2023). The spiritual successor to Forge for modern versions. Active development, recommended for Forge-style mods on 1.20.2+.

**Supported Minecraft Versions:**
- 1.20.1
- 1.20.2
- 1.20.3
- 1.20.4
- 1.20.5
- 1.20.6
- 1.21
- 1.21.1
- 1.21.2
- 1.21.3
- 1.21.4
- 1.21.5
- 1.21.6
- 1.21.7
- 1.21.8
- 1.21.9
- 1.21.10
- 1.21.11

**Notes:**
- For 1.20.1, maintains Forge API compatibility
- Starting 1.20.2+, API diverges from Forge
- Uses NeoGradle or ModDevGradle for development

---

### 2. Minecraft Forge
**Description:** The original and most established mod loader, created in 2011. Extensive mod library, especially for content-heavy mods.

**Supported Minecraft Versions:**
- 1.1
- 1.2.3, 1.2.4, 1.2.5
- 1.3.2
- 1.4.0, 1.4.1, 1.4.2, 1.4.3, 1.4.4, 1.4.5, 1.4.6, 1.4.7
- 1.5, 1.5.1, 1.5.2
- 1.6.1, 1.6.2, 1.6.3, 1.6.4
- 1.7.2, 1.7.10
- 1.8, 1.8.8, 1.8.9
- 1.9, 1.9.4
- 1.10, 1.10.2
- 1.11, 1.11.2
- 1.12, 1.12.1, 1.12.2
- 1.13.2
- 1.14.2, 1.14.3, 1.14.4
- 1.15, 1.15.1, 1.15.2
- 1.16.1, 1.16.2, 1.16.3, 1.16.4, 1.16.5
- 1.17.1
- 1.18, 1.18.1, 1.18.2
- 1.19, 1.19.1, 1.19.2, 1.19.3, 1.19.4
- 1.20, 1.20.1, 1.20.2, 1.20.3, 1.20.4, 1.20.6
- 1.21, 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7

**Notes:**
- Legacy support continues but active development has moved to NeoForge
- 1.20.1 and below recommended for Forge; 1.20.2+ use NeoForge

---

### 3. Fabric
**Description:** Lightweight, modular mod loader. Fast updates, popular for performance and QoL mods. Created 2016, publicly announced 2018.

**Supported Minecraft Versions (via Fabric API):**
- 1.14, 1.14.1, 1.14.2, 1.14.3, 1.14.4
- 1.15, 1.15.1, 1.15.2
- 1.16, 1.16.1, 1.16.2, 1.16.3, 1.16.4, 1.16.5
- 1.17, 1.17.1
- 1.18, 1.18.1, 1.18.2
- 1.19, 1.19.1, 1.19.2, 1.19.3, 1.19.4
- 1.20, 1.20.1, 1.20.2, 1.20.3, 1.20.4, 1.20.5, 1.20.6
- 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11

**Notes:**
- Requires Fabric Loader + Fabric API (separate downloads)
- Fabric Loader is version-agnostic; Fabric API is version-specific
- Supports snapshots

---

### 4. Quilt
**Description:** Fork of Fabric (2022) with focus on modularity, community governance, and developer ergonomics. Can run most Fabric mods.

**Supported Minecraft Versions (via Quilted Fabric API / QSL):**
- 1.18.2
- 1.19, 1.19.1, 1.19.2, 1.19.3, 1.19.4
- 1.20, 1.20.1, 1.20.2, 1.20.4, 1.20.6
- 1.21

**Notes:**
- Requires Quilt Loader + Quilted Fabric API (QFAPI) / QSL
- Can load most Fabric mods natively
- QSL is stripped down for 1.20.6+ (only qsl_base and crash_info)
- Still in beta development

---

### 5. Legacy Fabric
**Description:** Fabric for versions before 1.14. Maintains parity with upstream Fabric.

**Supported Minecraft Versions:**
- 1.3.2
- 1.4.7
- 1.5.2
- 1.6.4
- 1.7.10
- 1.8, 1.8.9
- 1.9.4
- 1.10.2
- 1.11.2
- 1.12.2

**Notes:**
- Requires Legacy Fabric Loader + Legacy Fabric API
- Separate project from modern Fabric
- Popular versions: 1.7.10, 1.8.9, 1.12.2

---

## CROSS-COMPATIBILITY LAYERS

### Sinytra Connector
**Description:** Translation layer allowing Fabric mods to run on NeoForge.

**Supported Versions:**
- 1.20.1 (LTS, critical bugfixes only)
- 1.21.1 (Primary supported version)

**Requirements:**
- NeoForge
- Sinytra Connector
- Forgified Fabric API (NOT regular Fabric API)

---

## COMPATIBILITY MATRIX

| From \ To | Forge | NeoForge | Fabric | Quilt | Legacy Fabric |
|-----------|-------|----------|--------|-------|---------------|
| **Forge** | ✅ | ⚠️ 1.20.1 only | ❌ | ❌ | ❌ |
| **NeoForge** | ⚠️ 1.20.1 only | ✅ | ⚠️ via Connector | ❌ | ❌ |
| **Fabric** | ❌ | ⚠️ via Connector | ✅ | ✅ mostly | ❌ |
| **Quilt** | ❌ | ❌ | ❌ | ✅ | ❌ |
| **Legacy Fabric** | ❌ | ❌ | ❌ | ❌ | ✅ |

---

## COMPILATION TARGET MATRIX

For each mod loader, compile for these specific versions:

### NEOFORGE TARGETS (18 versions)
```
1.20.1, 1.20.2, 1.20.3, 1.20.4, 1.20.5, 1.20.6,
1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11
```

### FORGE TARGETS (56 versions)
```
1.1,
1.2.3, 1.2.4, 1.2.5,
1.3.2,
1.4.0, 1.4.1, 1.4.2, 1.4.3, 1.4.4, 1.4.5, 1.4.6, 1.4.7,
1.5, 1.5.1, 1.5.2,
1.6.1, 1.6.2, 1.6.3, 1.6.4,
1.7.2, 1.7.10,
1.8, 1.8.8, 1.8.9,
1.9, 1.9.4,
1.10, 1.10.2,
1.11, 1.11.2,
1.12, 1.12.1, 1.12.2,
1.13.2,
1.14.2, 1.14.3, 1.14.4,
1.15, 1.15.1, 1.15.2,
1.16.1, 1.16.2, 1.16.3, 1.16.4, 1.16.5,
1.17.1,
1.18, 1.18.1, 1.18.2,
1.19, 1.19.1, 1.19.2, 1.19.3, 1.19.4,
1.20, 1.20.1
```
*(Note: Avoid 1.20.2+ for Forge; use NeoForge instead)*

### FABRIC TARGETS (37 versions)
```
1.14, 1.14.1, 1.14.2, 1.14.3, 1.14.4,
1.15, 1.15.1, 1.15.2,
1.16, 1.16.1, 1.16.2, 1.16.3, 1.16.4, 1.16.5,
1.17, 1.17.1,
1.18, 1.18.1, 1.18.2,
1.19, 1.19.1, 1.19.2, 1.19.3, 1.19.4,
1.20, 1.20.1, 1.20.2, 1.20.3, 1.20.4, 1.20.5, 1.20.6,
1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11
```

### QUILT TARGETS (11 versions)
```
1.18.2,
1.19, 1.19.1, 1.19.2, 1.19.3, 1.19.4,
1.20, 1.20.1, 1.20.2, 1.20.4, 1.20.6,
1.21
```

### LEGACY FABRIC TARGETS (11 versions)
```
1.3.2,
1.4.7,
1.5.2,
1.6.4,
1.7.10,
1.8, 1.8.9,
1.9.4,
1.10.2,
1.11.2,
1.12.2
```

---

## TOTAL COMPILATION TARGETS

| Loader | Version Count |
|--------|---------------|
| NeoForge | 18 |
| Forge | 56 |
| Fabric | 43 |
| Quilt | 11 |
| Legacy Fabric | 11 |
| **TOTAL** | **139 unique builds** |

---

## BUILD TOOLING REQUIREMENTS

### For Forge (Pre-1.17)
- ForgeGradle
- MCP/Searge mappings
- Java 8 (1.12.2 and below), Java 16 (1.17)

### For Forge (1.17+) / NeoForge
- ForgeGradle / NeoGradle / ModDevGradle
- Mojang mappings (official)
- Java 17 (1.17-1.20.4), Java 21 (1.20.5+)

### For Fabric / Quilt
- Fabric Loom / Quilt Loom
- Yarn or Mojang mappings
- Intermediary for cross-version compatibility
- Java 17 (1.17-1.20.4), Java 21 (1.20.5+)

### For Legacy Fabric
- Legacy Fabric Loom
- Legacy Yarn mappings
- Java 8 (1.12.2 and below)

---

## RECOMMENDED BUILD STRATEGY

1. **Use a multi-loader template** (e.g., Architectury, Sinytra's Multi-loader)
2. **Separate common code** from loader-specific code
3. **Build in version groups:**
   - Modern (1.20.5+): NeoForge + Fabric
   - Stable (1.18-1.20.4): Forge + NeoForge + Fabric + Quilt
   - Legacy Modern (1.14-1.17): Forge + Fabric
   - Legacy (pre-1.14): Forge + Legacy Fabric

4. **Automate with CI/CD** (GitHub Actions matrix builds)

---

## OUTPUT FILE NAMING CONVENTION

```
{modid}-{modversion}-{loader}-{mcversion}.jar

Examples:
mymod-1.0.0-forge-1.12.2.jar
mymod-1.0.0-fabric-1.20.1.jar
mymod-1.0.0-neoforge-1.21.1.jar
mymod-1.0.0-quilt-1.20.1.jar
mymod-1.0.0-legacyfabric-1.8.9.jar
```

---

## INSTRUCTIONS FOR AI

When compiling the mod:
1. Identify which features require loader-specific APIs
2. Create abstraction layers for cross-loader compatibility
3. Generate separate build configurations for each loader/version combination
4. Handle API differences between versions (breaking changes, renamed classes, etc.)
5. Include appropriate dependencies (Fabric API, Forge dependencies, etc.)
6. Generate proper mod metadata files (fabric.mod.json, mods.toml, mcmod.info, quilt.mod.json)
7. Test compilation for each target before release
8. Document any version-specific limitations or features

---

*Last Updated: January 2026*
*Data sourced from: NeoForged.net, MinecraftForge.net, FabricMC.net, QuiltMC.org, LegacyFabric.net, Modrinth, CurseForge*
