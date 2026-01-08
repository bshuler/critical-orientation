# Critical Orientation - Modernization & Multi-Platform Expansion Plan

## Executive Summary

This document outlines transforming Critical Orientation from a single Minecraft 1.17.1 Fabric mod into a comprehensive multi-loader, multi-version client-side mod with robust CI/CD infrastructure.

**Core Strategic Decisions:**
- **Multi-Version**: Stonecutter preprocessor enabling single codebase with version-conditional paths
- **Multi-Loader**: Architectury + Stonecraft supporting Fabric, Forge, NeoForge, Quilt
- **Scope**: Client-side functionality exclusively (direction snapping for ice boat placement)

---

## Phase 1: Infrastructure & Branch Setup ✅ COMPLETED

### 1.1 Git Branch Structure
```
main  → Production releases (triggers CurseForge/Modrinth deployment)
test  → Integration testing branch
dev   → Active development branch
```

### 1.2 Completed Tasks
- [x] Development branch creation
- [x] `.gitignore` modernization
- [x] CI/CD workflow initialization

---

## Phase 2: Upgrade to Latest Minecraft & Fabric ✅ COMPLETED

### 2.1 Version Progression
| Component | Previous | Current |
|-----------|----------|---------|
| Minecraft | 1.17.1 | 1.21.4 |
| Fabric Loader | 0.11.6 | 0.16.10 |
| Fabric Loom | 0.9-SNAPSHOT | 1.9-SNAPSHOT |
| Java | 16 | 21 |
| Gradle | 6.5 | 8.11.1 |
| Yarn Mappings | 1.17.1+build.39 | 1.21.4+build.8 |
| Fabric API | 0.39.2+1.17 | 0.119.2+1.21.4 |

### 2.2 Completed Tasks
- [x] Gradle wrapper updated to 8.11.1
- [x] build.gradle modernized with current Fabric Loom
- [x] Migration from jcenter() to mavenCentral()
- [x] Java 21 implementation
- [x] Yarn mappings updated to 1.21.4+build.8
- [x] Fabric API updated to 0.119.2+1.21.4
- [x] Migrate deprecated `FabricKeyBinding` to modern `KeyBindingHelper`
- [x] Migrate deprecated `ClientTickCallback` to `ClientTickEvents`
- [x] Update player yaw setting API (using setYaw/setHeadYaw/setBodyYaw)
- [x] fabric.mod.json configuration update
- [x] Split source sets (client/main) implemented

### 2.3 API Migration Notes

**Keybinding Registration (Completed):**
```java
// Old (deprecated in 1.17+)
FabricKeyBinding.Builder.create(...).build();
KeyBindingRegistry.INSTANCE.addCategory(categoryName);
KeyBindingRegistry.INSTANCE.register(keyBinding);

// New (1.18+) - IMPLEMENTED
KeyBinding keyBinding = new KeyBinding(...);
KeyBindingHelper.registerKeyBinding(keyBinding);
```

**Event Callbacks (Completed):**
```java
// Old (deprecated)
ClientTickCallback.EVENT.register(e -> { ... });

// New (1.17+) - IMPLEMENTED
ClientTickEvents.END_CLIENT_TICK.register(client -> { ... });
```

---

## Phase 3: Comprehensive Testing Framework ✅ COMPLETED

### 3.1 Testing Infrastructure
- [x] JUnit 5 dependency integration
- [x] Test source set creation (`src/test/java`)
- [x] Yaw normalization unit tests
- [x] Direction rounding unit tests

### 3.2 Test Coverage
- [x] `normalizeHeadYaw()` boundary testing (-180 to 180 range)
- [x] `roundYaw()` cardinal direction snapping (0, 90, 180, -90)
- [x] `roundYaw()` intercardinal direction snapping (45, 135, -45, -135)
- [x] Edge case handling (360+, -360-, exact boundaries)
- [x] Integration tests (normalize then round)

---

## Phase 4: CI/CD Pipeline ✅ COMPLETED

### 4.1 Workflow Implementation
```
.github/workflows/
├── build.yml   → Build & test on dev/test/main branch pushes
└── release.yml → CurseForge & Modrinth deployment on version tags
```

### 4.2 Required Configuration Secrets
```yaml
CURSEFORGE_ID      → Project identifier (already on CurseForge)
CURSEFORGE_TOKEN   → API authentication
MODRINTH_ID        → Project identifier
MODRINTH_TOKEN     → API authentication
```

---

## Phase 5: Multi-Version Support (Stonecutter)

### 5.1 Implementation Strategy: Stonecutter Preprocessor

[Stonecutter](https://stonecutter.kikugie.dev/) enables conditional compilation within unified source:
- Single codebase with version-specific conditional blocks
- Preprocessor directives for compilation branching
- Semantic version comparisons supported
- Compatible with Fabric and Forge ecosystems

### 5.2 Target Minecraft Versions

| Version | Release Name | Priority | Java Requirement |
|---------|--------------|----------|------------------|
| 1.21.x | Tricky Trials | High (latest) | 21 |
| 1.20.x | Trails & Tales | High | 17-21 |
| 1.19.x | The Wild Update | Medium | 17 |
| 1.18.x | Caves & Cliffs Part 2 | Medium | 17 |
| 1.17.x | Caves & Cliffs Part 1 | Low (legacy) | 16 |

### 5.3 Stonecutter Build Configuration

```kotlin
// stonecutter.gradle.kts
stonecutter {
    versions("1.21.4", "1.20.6", "1.20.1", "1.19.4", "1.18.2", "1.17.1")
}
```

### 5.4 Version-Conditional Code Pattern

```java
//? if >=1.18 {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//?} else {
/*import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;*/
//?}
```

---

## Phase 6: Multi-Loader Architecture (Architectury + Stonecraft)

### 6.1 Target Client-Side Mod Loaders

| Loader | Support Level | Implementation Notes |
|--------|----------------|----------------------|
| Fabric | Primary | Current base implementation |
| Quilt | High | Fabric-compatible with minimal modifications |
| NeoForge | High | Contemporary Forge successor (1.20.4+) |
| Forge | Medium | Legacy compatibility (1.20.1 and earlier) |

### 6.2 Chosen Architecture: Stonecraft

[Stonecraft](https://stonecraft.meza.gg/) combines Stonecutter and Architectury capabilities:
- Reduces ~500 build configuration lines to single plugin
- Automatic Fabric, Forge, NeoForge handling
- Established, versioned plugin framework

### 6.3 Project Directory Organization

```
critical-orientation/
├── .github/workflows/
│   ├── build.yml
│   └── release.yml
├── src/
│   └── main/
│       ├── java/net/critical/orientation/
│       │   ├── Orientation.java              # Loader entry point
│       │   ├── OrientationCommon.java        # Shared initialization
│       │   ├── keybind/
│       │   │   └── OrientationKeyBind.java   # Keybind and snapping logic
│       │   └── platform/
│       │       └── PlatformHelper.java       # Loader abstraction
│       └── resources/
│           ├── fabric.mod.json               # Fabric metadata
│           ├── META-INF/mods.toml            # Forge/NeoForge metadata
│           └── assets/orientation/
│               └── lang/
│                   └── en_us.json
├── versions/                                  # Stonecutter configurations
│   ├── 1.21.4-fabric/
│   ├── 1.21.4-neoforge/
│   ├── 1.20.6-fabric/
│   ├── 1.20.6-forge/
│   └── ...
├── build.gradle.kts
├── settings.gradle.kts
├── stonecutter.gradle.kts
├── gradle.properties
├── CLAUDE.md
└── PLAN.md
```

### 6.4 Platform Abstraction Implementation

```java
// PlatformHelper.java - Architectury-provided interface
public class PlatformHelper {
    public static boolean isFabric() {
        return Platform.isFabric();
    }

    public static boolean isForge() {
        return Platform.isForge();
    }

    public static Path getConfigDir() {
        return Platform.getConfigFolder();
    }
}
```

### 6.5 Loader-Specific Entry Points

**Fabric/Quilt Implementation:**
```java
public class OrientationFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        OrientationCommon.init();
    }
}
```

**Forge/NeoForge Implementation:**
```java
@Mod("orientation")
public class OrientationForge {
    public OrientationForge() {
        FMLJavaModLoadingContext.get().getModEventBus()
            .addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        OrientationCommon.init();
    }
}
```

### 6.6 Cross-Loader Keybind Registration

**Fabric:** `KeyBindingHelper.registerKeyBinding()`
**Forge/NeoForge:** `RegisterKeyMappingsEvent` subscription

```java
// Shared keybind logic
public class OrientationKeyBind {
    public static KeyBinding keyBinding;

    public static void register() {
        //? if fabric {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(...));
        //?} else if forge || neoforge {
        /*// Forge registration handled via event*/
        //?}
    }

    public static void onTick(MinecraftClient client) {
        if (keyBinding.wasPressed() && client.player != null) {
            double yaw = client.player.getHeadYaw();
            yaw = roundYaw(normalizeHeadYaw(yaw));
            client.player.setYaw((float) yaw);
            client.player.setHeadYaw((float) yaw);
            client.player.setBodyYaw((float) yaw);
        }
    }
}
```

---

## Phase 7: Implementation Roadmap

### Stage 1: Foundation ✅ COMPLETED
1. [x] Branch structure establishment
2. [x] `.gitignore` modernization
3. [x] Latest Minecraft/Fabric upgrade (1.21.4)
4. [x] Deprecated API migration (`FabricKeyBinding` → `KeyBindingHelper`)
5. [x] Deprecated API migration (`ClientTickCallback` → `ClientTickEvents`)
6. [x] CI/CD pipeline setup
7. [x] Testing framework implementation

### Stage 2: Multi-Loader ✅ COMPLETED
8. [x] Stonecraft/Architectury project structure
9. [x] Platform abstraction layer development (OrientationCommon.java)
10. [x] Fabric loader module creation
11. [x] NeoForge loader module creation
12. [x] Forge loader module (1.20.1 support)
13. [x] CI/CD multi-loader build configuration

### Stage 3: Multi-Version ✅ COMPLETED
14. [x] Stonecutter version management configuration
15. [x] 1.20.1 support addition (Fabric + Forge)
16. [x] 1.19.4 support addition (Fabric + Forge)
17. [x] 1.18.2 support addition (Fabric + Forge)
18. [x] Build configuration verified (tests run in CI/CD)

### Stage 4: Polish ✅ READY FOR RELEASE
19. [~] Configuration system integration (SKIPPED - keybind already customizable via Options > Controls)
20. [x] Remove debug `test()` method and `System.out.println()` statements
21. [x] Documentation development (README, CLAUDE.md updated)
22. [ ] CurseForge update and Modrinth publication (USER ACTION: set secrets, then `git tag v2.0.0`)

---

## Appendix A: Key Resources

### Documentation
- [Stonecutter Documentation](https://stonecutter.kikugie.dev/)
- [Stonecraft](https://stonecraft.meza.gg/)
- [Architectury Documentation](https://docs.architectury.dev/)
- [Fabric Documentation](https://docs.fabricmc.net/)
- [NeoForge Documentation](https://docs.neoforged.net/)
- [mc-publish GitHub Action](https://github.com/marketplace/actions/mc-publish)

### Publishing Platforms
- [CurseForge - Critical Orientation](https://www.curseforge.com/minecraft/mc-mods/critical-orientation)
- [Modrinth Mods](https://modrinth.com/mods)

### Reference Implementations
- Stonecraft GitHub repository contains templates and sample projects

---

## Appendix B: Version Compatibility Matrix (CURRENT - January 2026)

| MC Version | Fabric | NeoForge | Forge | Java | Notes |
|------------|--------|----------|-------|------|-------|
| 1.21.11 | ✅ | ✅ | ❌ | 21 | Latest (Dec 2025) |
| 1.21.4 | ✅ | ✅ | ❌ | 21 | The Garden Awakens |
| 1.21.1 | ✅ | ✅ | ❌ | 21 | Popular LTS |
| 1.21 | ✅ | ✅ | ❌ | 21 | Tricky Trials |
| 1.20.6 | ✅ | ✅ | ❌ | 21 | |
| 1.20.5 | ✅ | ❌ | ❌ | 21 | NeoForge beta has Java conflicts |
| 1.20.4 | ✅ | ❌ | ❌ | 17 | Forge missing bootstrap-dev dep |
| 1.20.3 | ✅ | ❌ | ❌ | 17 | Forge missing bootstrap-dev dep |
| 1.20.1 | ✅ | ❌ | ✅ | 17 | Most popular modded version |
| 1.20 | ✅ | ❌ | ✅ | 17 | Trails & Tales |
| 1.19.4 | ✅ | ❌ | ✅ | 17 | |
| 1.18.2 | ✅ | ❌ | ✅ | 17 | |

**Total: 21 JARs** (12 Fabric + 5 NeoForge + 4 Forge)

Note: NeoForge branched from Forge at 1.20.2. Earlier versions have Forge-only support.

---

## Appendix C: Keybinding API Changes by Version

| Version | Keybinding API | Event API |
|---------|---------------|-----------|
| 1.21.x | `KeyBindingHelper` | `ClientTickEvents` |
| 1.20.x | `KeyBindingHelper` | `ClientTickEvents` |
| 1.19.x | `KeyBindingHelper` | `ClientTickEvents` |
| 1.18.x | `KeyBindingHelper` | `ClientTickEvents` |
| 1.17.x | `FabricKeyBinding` (deprecated) | `ClientTickCallback` (deprecated) |

Key API transition points:
- `FabricKeyBinding.Builder.create()` (1.17) → `new KeyBinding()` + `KeyBindingHelper.registerKeyBinding()` (1.18+)
- `ClientTickCallback.EVENT` (1.17) → `ClientTickEvents.END_CLIENT_TICK` (1.18+)
- `KeyBindingRegistry.INSTANCE.addCategory()` (1.17) → Categories auto-created (1.18+)

---

## Appendix D: Code Issues Addressed ✅

### MyKeyBind.java → OrientationKeyBind.java (Refactored)
1. ✅ **Debug code**: Removed `test()` method and associated `System.out.println()`
2. ✅ **Deprecated APIs**: Migrated to `KeyBindingHelper` and `ClientTickEvents`
3. ✅ **Double semicolon**: Cleaned up
4. ✅ **Commented code**: Removed debug loops

### Orientation.java → OrientationClient.java (Refactored)
1. ✅ **Debug output**: Removed `System.out.println("Critical Orientation Mod started.")`

### fabric.mod.json (Updated)
1. ✅ **Contact URLs**: Updated to actual GitHub repository and CurseForge
2. ✅ **Icon path**: Fixed mod ID reference
3. ✅ **Environment**: Set to "client" for client-side only
4. ✅ **Entry point**: Changed to client entrypoint

---

## Current State (January 8, 2026)

### Build Status: ✅ PASSING

**Dev Release**: https://github.com/bshuler/critical-orientation/releases/tag/dev-claude-claude-md-mk408y9lxsjoeowm-5NyUs

### JAR Artifacts (21 total)

**Fabric (12 JARs)**:
- critical-orientation-fabric-2.0.0+mc1.21.11.jar
- critical-orientation-fabric-2.0.0+mc1.21.4.jar
- critical-orientation-fabric-2.0.0+mc1.21.1.jar
- critical-orientation-fabric-2.0.0+mc1.21.jar
- critical-orientation-fabric-2.0.0+mc1.20.6.jar
- critical-orientation-fabric-2.0.0+mc1.20.5.jar
- critical-orientation-fabric-2.0.0+mc1.20.4.jar
- critical-orientation-fabric-2.0.0+mc1.20.3.jar
- critical-orientation-fabric-2.0.0+mc1.20.1.jar
- critical-orientation-fabric-2.0.0+mc1.20.jar
- critical-orientation-fabric-2.0.0+mc1.19.4.jar
- critical-orientation-fabric-2.0.0+mc1.18.2.jar

**NeoForge (5 JARs)**:
- critical-orientation-neoforge-2.0.0+mc1.21.11.jar
- critical-orientation-neoforge-2.0.0+mc1.21.4.jar
- critical-orientation-neoforge-2.0.0+mc1.21.1.jar
- critical-orientation-neoforge-2.0.0+mc1.21.jar
- critical-orientation-neoforge-2.0.0+mc1.20.6.jar

**Forge (4 JARs)**:
- critical-orientation-forge-2.0.0+mc1.20.1.jar
- critical-orientation-forge-2.0.0+mc1.20.jar
- critical-orientation-forge-2.0.0+mc1.19.4.jar
- critical-orientation-forge-2.0.0+mc1.18.2.jar

### CI/CD Pipeline

- **build.yml**: Triggers on push to main/master/test/dev/claude/** branches
  - Builds all 21 JARs
  - Runs tests on Fabric versions
  - Creates dev/test release with all artifacts

- **release.yml**: Triggers on version tags (v*)
  - Builds all versions
  - Publishes to CurseForge and Modrinth
  - Creates GitHub release

### Key Files Modified

| File | Purpose |
|------|---------|
| `settings.gradle.kts` | Defines 12 MC versions × loaders |
| `build.gradle.kts` | Stonecraft config + JAR naming |
| `gradle.properties` | Mod metadata |
| `versions/dependencies/*.properties` | Per-version dependency configs |
| `.github/workflows/build.yml` | CI build + dev release |
| `.github/workflows/release.yml` | Production release |

### Known Limitations

1. **MC 1.20.3/1.20.4 Forge**: Missing `net.minecraftforge:bootstrap-dev:2.0.0` dependency
2. **MC 1.20.5 NeoForge**: Java version conflicts (NeoForge only had beta releases)
3. **yarn-mappings-patch**: Only available for specific versions, limits some loader combinations

---

## Next Steps

### Immediate (Before Release)

1. **[ ] Configure Publishing Secrets**
   - Add `CURSEFORGE_ID` secret to GitHub repo
   - Add `CURSEFORGE_TOKEN` secret to GitHub repo
   - Add `MODRINTH_ID` secret to GitHub repo
   - Add `MODRINTH_TOKEN` secret to GitHub repo

2. **[ ] Test Dev Release**
   - Download JARs from dev release
   - Test in Minecraft with each loader
   - Verify keybind works (`\` key snaps direction)

3. **[ ] Merge to Main**
   - Create PR from `claude/claude-md-mk408y9lxsjoeowm-5NyUs` to `main`
   - Review changes
   - Merge

4. **[ ] Create Production Release**
   ```bash
   git checkout main
   git tag v2.0.0
   git push origin v2.0.0
   ```

### Post-Release

5. **[ ] Verify CurseForge Publication**
   - Check all 21 files uploaded
   - Verify version compatibility tags
   - Update mod description if needed

6. **[ ] Verify Modrinth Publication**
   - Check all 21 files uploaded
   - Verify loader tags
   - Add changelog

7. **[ ] Update Documentation**
   - Update README.md with new version support
   - Update CurseForge/Modrinth descriptions
   - Add installation instructions per loader

### Future Enhancements

8. **[ ] Add More Versions** (if requested)
   - MC 1.21.2, 1.21.3, 1.21.5+ when mapping patches available
   - Legacy versions (1.17.1, 1.16.5) if demand exists

9. **[ ] Quilt Support**
   - Add Quilt loader builds (Fabric-compatible)
   - Test with Quilted Fabric API

10. **[ ] Configuration Options**
    - Add config file for custom snap angles (e.g., 30° increments)
    - Add toggle for visual/audio feedback on snap

11. **[ ] Localization**
    - Add translations for key names
    - Community translations
