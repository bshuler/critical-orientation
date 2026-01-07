# Critical Orientation - Modernization & Multi-Platform Expansion Plan

## Executive Summary

This document outlines transforming Critical Orientation from a single Minecraft 1.17.1 Fabric mod into a comprehensive multi-loader, multi-version client-side mod with robust CI/CD infrastructure.

**Core Strategic Decisions:**
- **Multi-Version**: Stonecutter preprocessor enabling single codebase with version-conditional paths
- **Multi-Loader**: Architectury + Stonecraft supporting Fabric, Forge, NeoForge, Quilt
- **Scope**: Client-side functionality exclusively (direction snapping for ice boat placement)

---

## Phase 1: Infrastructure & Branch Setup

### 1.1 Git Branch Structure
```
main  → Production releases (triggers CurseForge/Modrinth deployment)
test  → Integration testing branch
dev   → Active development branch
```

### 1.2 Tasks
- [ ] Development branch creation
- [ ] `.gitignore` modernization
- [ ] CI/CD workflow initialization

---

## Phase 2: Upgrade to Latest Minecraft & Fabric

### 2.1 Version Progression
| Component | Current | Target |
|-----------|---------|--------|
| Minecraft | 1.17.1 | 1.21.4 |
| Fabric Loader | 0.11.6 | 0.16.10 |
| Fabric Loom | 0.9-SNAPSHOT | 1.9-SNAPSHOT |
| Java | 16 | 21 |
| Gradle | 7.x | 8.11.1 |
| Yarn Mappings | 1.17.1+build.39 | 1.21.4+build.8 |
| Fabric API | 0.39.2+1.17 | 0.114.0+1.21.4 |

### 2.2 Tasks
- [ ] Gradle wrapper updated to 8.11.1
- [ ] build.gradle modernized with current Fabric Loom
- [ ] Migration from jcenter() to mavenCentral()
- [ ] Java 21 implementation
- [ ] Yarn mappings updated to 1.21.4+build.8
- [ ] Fabric API updated to 0.114.0+1.21.4
- [ ] Migrate deprecated `FabricKeyBinding` to modern `KeyBindingHelper`
- [ ] Migrate deprecated `ClientTickCallback` to `ClientTickEvents`
- [ ] Update `refreshPositionAndAngles()` API if changed
- [ ] fabric.mod.json configuration update

### 2.3 API Migration Notes

**Keybinding Registration (Critical Change):**
```java
// Old (deprecated in 1.17+)
FabricKeyBinding.Builder.create(...).build();
KeyBindingRegistry.INSTANCE.addCategory(categoryName);
KeyBindingRegistry.INSTANCE.register(keyBinding);

// New (1.18+)
KeyBinding keyBinding = new KeyBinding(...);
KeyBindingHelper.registerKeyBinding(keyBinding);
```

**Event Callbacks (Critical Change):**
```java
// Old (deprecated)
ClientTickCallback.EVENT.register(e -> { ... });

// New (1.17+)
ClientTickEvents.END_CLIENT_TICK.register(client -> { ... });
```

---

## Phase 3: Comprehensive Testing Framework

### 3.1 Testing Infrastructure
- [ ] JUnit 5 dependency integration
- [ ] Test source set creation (`src/test/java`)
- [ ] Yaw normalization unit tests
- [ ] Direction rounding unit tests

### 3.2 Test Coverage
- [ ] `normalizeHeadYaw()` boundary testing (-180 to 180 range)
- [ ] `roundYaw()` cardinal direction snapping (0, 90, 180, -90)
- [ ] `roundYaw()` intercardinal direction snapping (45, 135, -45, -135)
- [ ] Edge case handling (360+, -360-, exact boundaries)

### 3.3 Example Test Cases
```java
@Test
void testNormalizeHeadYaw() {
    assertEquals(0, MyKeyBind.normalizeHeadYaw(0));
    assertEquals(0, MyKeyBind.normalizeHeadYaw(360));
    assertEquals(0, MyKeyBind.normalizeHeadYaw(-360));
    assertEquals(90, MyKeyBind.normalizeHeadYaw(450));
    assertEquals(-90, MyKeyBind.normalizeHeadYaw(-450));
}

@Test
void testRoundYawCardinal() {
    assertEquals(0, MyKeyBind.roundYaw(10));      // North
    assertEquals(90, MyKeyBind.roundYaw(80));     // East
    assertEquals(180, MyKeyBind.roundYaw(170));   // South
    assertEquals(-90, MyKeyBind.roundYaw(-80));   // West
}

@Test
void testRoundYawIntercardinal() {
    assertEquals(45, MyKeyBind.roundYaw(35));     // NE
    assertEquals(135, MyKeyBind.roundYaw(125));   // SE
    assertEquals(-45, MyKeyBind.roundYaw(-35));   // NW
    assertEquals(-135, MyKeyBind.roundYaw(-125)); // SW
}
```

---

## Phase 4: CI/CD Pipeline

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

### 4.3 Build Workflow Example
```yaml
name: Build
on:
  push:
    branches: [main, test, dev]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Build with Gradle
        run: ./gradlew build
      - name: Run tests
        run: ./gradlew test
      - name: Upload artifacts
        uses: actions/upload-artifact@v4
        with:
          name: critical-orientation
          path: build/libs/*.jar
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
            client.player.refreshPositionAndAngles(
                client.player.getX(),
                client.player.getY(),
                client.player.getZ(),
                (float) yaw,
                client.player.getPitch()
            );
        }
    }
}
```

---

## Phase 7: Implementation Roadmap

### Stage 1: Foundation
1. [ ] Branch structure establishment
2. [ ] `.gitignore` modernization
3. [ ] Latest Minecraft/Fabric upgrade (1.21.4)
4. [ ] Deprecated API migration (`FabricKeyBinding` → `KeyBindingHelper`)
5. [ ] Deprecated API migration (`ClientTickCallback` → `ClientTickEvents`)
6. [ ] CI/CD pipeline setup
7. [ ] Testing framework implementation

### Stage 2: Multi-Loader
8. [ ] Stonecraft/Architectury project structure
9. [ ] Platform abstraction layer development
10. [ ] Fabric loader module creation
11. [ ] NeoForge loader module creation
12. [ ] Forge loader module (1.20.1 support)
13. [ ] CI/CD multi-loader build configuration

### Stage 3: Multi-Version
14. [ ] Stonecutter version management configuration
15. [ ] 1.20.x support addition
16. [ ] 1.19.x support addition
17. [ ] 1.18.x support addition
18. [ ] Comprehensive version/loader combination testing

### Stage 4: Polish
19. [ ] Configuration system integration (optional keybind customization already exists)
20. [ ] Remove debug `test()` method and `System.out.println()` statements
21. [ ] Documentation development and release preparation
22. [ ] CurseForge update and Modrinth publication

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

## Appendix B: Version Compatibility Matrix

| MC Version | Fabric | NeoForge | Forge | Java |
|------------|--------|----------|-------|------|
| 1.21.4 | ✅ | ✅ | ❌ | 21 |
| 1.21.1 | ✅ | ✅ | ❌ | 21 |
| 1.20.6 | ✅ | ✅ | ❌ | 21 |
| 1.20.4 | ✅ | ✅ | ❌ | 17 |
| 1.20.1 | ✅ | ❌ | ✅ | 17 |
| 1.19.4 | ✅ | ❌ | ✅ | 17 |
| 1.18.2 | ✅ | ❌ | ✅ | 17 |
| 1.17.1 | ✅ | ❌ | ✅ | 16 |

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

## Appendix D: Current Code Issues to Address

### MyKeyBind.java
1. **Debug code**: Remove `test()` method and associated `System.out.println()` for production
2. **Deprecated APIs**: `FabricKeyBinding` and `ClientTickCallback` need migration
3. **Double semicolon**: Line 18 has `;;` which should be cleaned up
4. **Commented code**: Remove or address commented debug loops (lines 31-36)

### Orientation.java
1. **Debug output**: Remove `System.out.println("Critical Orientation Mod started.")` for production

### fabric.mod.json
1. **Contact URLs**: Update homepage and sources to actual GitHub repository
2. **Icon path**: `assets/modid/cardinal_directions.png` references wrong mod ID
