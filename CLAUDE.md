# CLAUDE.md - Critical Orientation Minecraft Mod

## Project Overview

Critical Orientation is a multi-loader, multi-version Minecraft client-side mod. It allows players to snap their facing direction to the nearest cardinal or intercardinal point (N, NE, E, SE, S, SW, W, NW) with a keybind. This is primarily useful for precise ice boat placement.

This repo is **THE HOUSE TEMPLATE** for keeping all of Bert's Minecraft mods
building on every modern Minecraft version and loader, latest first. See
`PLAN.md` for the full porting history, Stonecutter mechanics learned while
building this matrix, and known limitations — read it before porting another
mod from this template or extending this matrix further.

## Supported Platforms

15 Minecraft versions, up to 2 loaders each = 30 subprojects. Full matrix
with per-cell status and known caveats is in `PLAN.md`. Summary:

| Range | Loaders |
|-------|---------|
| 1.18.2 - 1.20.1 | Fabric + Forge |
| 1.21.4 - 26.2 (latest stable) | Fabric + NeoForge |

Quilt is not built directly (documented as expected-compatible via Fabric API in `PLAN.md`).

## Tech Stack

- **Language**: Java (toolchain-selected per version: 17 through <1.20.6, 21 through <21.6, 25 from 21.6 on — see `javaVersion` logic in `build.gradle.kts`)
- **Build System**: Gradle 9.7.0 with Stonecraft (`gg.meza.stonecraft`) + Stonecutter (`dev.kikugie.stonecutter`)
- **Multi-Loader**: Stonecraft (Stonecutter + Architectury Loom combined)
- **Testing**: three tiers — JUnit 5 + JaCoCo (100% line/branch coverage enforced on
  `OrientationCommon`, the only genuinely headless-testable class), `fabric-loader-junit`
  loaded-game tests, and a `fabric-client-gametest-api-v1` client gametest that is the only
  thing covering `OrientationKeyBind` — see "Testing" below and `PLAN.md`
- **JDK**: only Temurin 21 is installed on this machine. Gradle toolchains / the foojay resolver auto-download whatever JDK a given subproject's Java version needs into `~/.gradle` — never install a system JDK for this.

## Repository Structure

```
critical-orientation/
├── .github/workflows/            # CI - editable and pushable (token has the workflow scope as of 2026-08-13)
│   ├── build.yml
│   └── release.yml
├── build.gradle.kts              # Stonecraft build configuration (shared across all subprojects)
├── settings.gradle.kts           # Stonecutter version/loader matrix definition (mc() calls, vcsVersion)
├── stonecutter.gradle.kts        # Active version marker - NEVER hand-edit, use the Gradle task (see below)
├── gradle.properties             # Mod metadata
├── LICENSE                       # CC0-1.0 license
├── README.md                     # User-facing documentation
├── CLAUDE.md                     # AI assistant guidance (this file)
├── PLAN.md                       # Modernization roadmap, porting notes, known limitations
├── versions/                     # One subproject dir per (mc-version)-(loader); generated + dependencies/
└── src/
    ├── main/java/net/critical/orientation/
    │   ├── OrientationClient.java    # Loader-specific entry point (all loaders, Stonecutter-conditional)
    │   ├── OrientationKeyBind.java   # Keybind registration + tick handling (all loaders, Stonecutter-conditional)
    │   └── OrientationCommon.java    # Shared, loader/version-agnostic yaw math
    ├── main/resources/
    │   ├── fabric.mod.json           # Fabric metadata (environment: client)
    │   ├── META-INF/
    │   │   ├── mods.toml             # Forge metadata (side = CLIENT)
    │   │   └── neoforge.mods.toml    # NeoForge metadata (side = CLIENT)
    │   └── assets/orientation/lang/en_us.json
    ├── test/java/net/critical/orientation/
    │   ├── OrientationCommonTest.java   # Unit tests against the shared, loader-agnostic logic
    │   └── LoadedGameTest.java          # Tier 1 - real bootstrapped Minecraft, Fabric cells only
    └── gametest/                        # Tier 3 - real client, never shipped in any jar
        ├── java/net/critical/orientation/gametest/OrientationClientGameTest.java
        └── resources/fabric.mod.json    # MANDATORY - declares the fabric-client-gametest entrypoint
```

**Note**: all code lives in `src/main/java` — there is no `src/client/java`.
This mod is 100% client-side on every loader, so the loader entry point and
keybind code sit directly in the main source set rather than a
Loom-only-and-therefore-non-portable client split. See `PLAN.md`'s "Source
set layout" section for why, and for the deliberate contrast with sibling
mods that make the opposite (also valid) choice.

## Key Components

### OrientationCommon.java (Shared Logic)
- Pure, loader/version-agnostic yaw math. No `Entity`/`Level` API usage at all.
- **`normalizeHeadYaw(double yaw)`**: Normalizes yaw to -180 to 180 range
- **`roundYaw(double yaw)`**: Rounds yaw to nearest 45-degree increment
- **`snapYaw(double yaw)`**: Combines normalize and round

### OrientationClient.java (Entry Point)
- Fabric: implements `ClientModInitializer`
- NeoForge/Forge: `@Mod`-annotated, hooks `FMLClientSetupEvent` via the mod event bus

### OrientationKeyBind.java (Keybinding)
- Registers backslash (`\`) key for direction snapping
- Fabric `>=26.1`: `KeyMappingHelper`; Fabric `<26.1`: `KeyBindingHelper` (package/class renamed)
- Forge `>=1.19` / NeoForge: `RegisterKeyMappingsEvent`; Forge `<1.19` (1.18.2 only): `ClientRegistry.registerKeyBinding()` directly
- `KeyMapping.Category` (MC `>=1.21.9`) vs plain `String` category (MC `<1.21.9`); `Identifier` (MC `>=1.21.11`) vs `ResourceLocation` (MC `<1.21.11`, `>=1.21.9`)
- Uses `keyBinding.consumeClick()` (not `wasPressed()`, which does not exist on `KeyMapping`)

Full boundary-by-boundary detail with jar/bytecode evidence is in `PLAN.md`.

## Build Commands

```bash
# Build every version/loader in the matrix
./gradlew chiseledBuild

# Build one target
./gradlew :26.2-fabric:build
./gradlew :1.18.2-forge:build

# Switch which subproject compiles from raw (un-generated) source for local dev/IDE support
./gradlew "Set active project to 26.2-fabric"

# Reset to the project's vcsVersion before committing (convention, see PLAN.md)
./gradlew "Reset active project"

# Run tests (green everywhere; NeoForge 1.21.4-1.21.8 need the junit-fml exclusion in build.gradle.kts - see PLAN.md)
./gradlew test

# Run Minecraft client (active version)
./gradlew runClient
```

## Testing / coverage

Tests run against the **active project only** (`26.2-fabric`) - this is a client mod with no
server component, and only `OrientationCommon`'s pure yaw math is genuinely testable headless.
`OrientationClient` and `OrientationKeyBind` touch real Minecraft/loader classes and are excluded
from coverage (see `PLAN.md` "Test coverage (Phase 2)" for the full reasoning).

```bash
# Run tests + coverage report + coverage-bar enforcement for the active project
./gradlew ":26.2-fabric:test" ":26.2-fabric:jacocoTestReport" ":26.2-fabric:jacocoTestCoverageVerification"

# Equivalent - check/build already depend on the coverage-verification task
./gradlew ":26.2-fabric:check"

# HTML report
open versions/26.2-fabric/build/reports/jacoco/test/html/index.html
```

The coverage bar is 100% line coverage on `OrientationCommon`, enforced by
`jacocoTestCoverageVerification` (which `check` depends on). If you add pure logic to
`OrientationCommon`, add real tests for it - the build will fail otherwise.

### Loaded-game tests (Tier 1)

`src/test/java/net/critical/orientation/LoadedGameTest.java` is different in kind
from the rest of the suite: it runs against a **real, bootstrapped Minecraft**, not
mocks. `net.fabricmc:fabric-loader-junit` stands a Fabric loader up inside the JUnit
worker, which makes it legal to call `SharedConstants.tryDetectVersion()` +
`Bootstrap.bootStrap()` in `@BeforeAll` and then assert against genuinely loaded game
data - the real item registry, and vanilla's own `Direction.fromYRot` yaw-to-facing
mapping.

That last point is the reason the file exists. `OrientationCommonTest` proves the
snap arithmetic is self-consistent; `LoadedGameTest` proves it agrees with the game.
A Minecraft version that quietly changed the yaw-to-facing convention would pass the
former and fail the latter.

- Fabric cells only (`//? if fabric` guards the whole file). NeoForge's equivalent
  bootstrap is `junit-fml`, and its supported loaded-test harness is ModDevGradle-only
  - unavailable under Architectury Loom. See the exclusion comment in
  `build.gradle.kts`.
- Verified green on **all 15 Fabric cells**, 1.18.2 through 26.2. Bootstrap costs
  ~3-7s per cell.
- The item registry moved `Registry.ITEM` -> `BuiltInRegistries.ITEM` in 1.19.3, so
  that one lookup carries a `//? if >=1.19.3` guard.

```bash
./gradlew ":26.2-fabric:test" --tests "*LoadedGameTest"
```

### Client gametest (Tier 3)

`src/gametest/java/net/critical/orientation/gametest/OrientationClientGameTest.java`
runs the mod in a **real Minecraft client** - real window, real GL context, real
world, real player - and presses a real backslash key through vanilla's real
keyboard handler.

```bash
unset JAVA_HOME && ./gradlew :26.2-fabric:runClientGameTest   # opens a real window
```

**This is the only tier that can see `OrientationKeyBind` at all.** Unit tests and
Tier 1 both stop at `OrientationCommon`'s arithmetic; `OrientationKeyBind` is on the
JaCoCo exclusion list and Tier 1 has no window, no input pipeline and no player.
Delete the `ClientTickEvents.END_CLIENT_TICK` registration or the
`KeyMappingHelper` call and every other check in this repo still passes while the
key does nothing. Do not remove this tier on the grounds that it covers "only a
keybind" - that keybind is the whole mod. (`PLAN.md`'s Tier 1 section used to argue
exactly that; it was wrong and now says so.)

Asserted in this order, so no step can pass vacuously: mod loaded ->
`key.orientation.snap` present in the live `Minecraft.options.keyMappings` and bound
to `key.keyboard.backslash` (checked *before* the world opens, so "never registered"
stays distinguishable from "registered but never ticks") -> nothing snaps without a
keypress -> 14 starting yaws each snap correctly on `yRot`, `yHeadRot` **and**
`yBodyRot` -> two presses in one tick are idempotent.

Things that will bite you when editing it:

- **`src/gametest/resources/fabric.mod.json` is mandatory.** Loom compiles the
  source set without it, but the harness discovers tests through the
  `fabric-client-gametest` entrypoint declared there. Without it the client boots to
  the title screen, quits, and reports **BUILD SUCCESSFUL having run nothing** -
  which is what the first attempt here did.
- **`TestServerContext#runCommand` swallows command failures** (bytecode-verified)
  and dispatches from world spawn, not the player. So staging is one
  `gamemode creative @p` plus a landmark `fill` wrapped in
  `execute as @p at @s run`, and *both* are confirmed by observable effect -
  `player.isCreative()`, and a client-side `GOLD_BLOCK` readback - never by the
  command having been issued. Do not add `gamerule` staging a yaw test does not need;
  26.2 renamed those rules and the sibling repos' six silent failures are why.
- **Press the raw keysym, not `pressKey(KeyMapping)`.** Passing the mod's own mapping
  proves only that the object exists. `GLFW_KEY_BACKSLASH` reaches it only via
  vanilla's real key-to-mapping map, which is rebuilt from `Options.keyMappings` - so
  an unregistered mapping genuinely receives nothing.
- **The world is featureless on purpose** (consistent settings), which made the first
  before/after screenshots pixel-identical and useless. Staging now builds a gold
  pillar 10 blocks due west - west *is* yaw 90 - and the pair is shot at 67.5 then
  90. Keep the landmark if you move the screenshots.
- **Exactly one Stonecutter branch in this file; do not add a second lightly.** The
  five gametest API types it uses are method-identical from v4.1.1 to v6.0.0
  (javap-verified). The one that did change is `TestSingleplayerContext`'s
  `getClientWorld`/`getClientLevel`/`getConnection` - avoid all three. The one
  branch that exists zeroes the spawn radius via `adjustSettings` on
  1.21.9-1.21.11 (two guarded forms, because 1.21.11 renamed `spawnRadius` to
  `respawnRadius` and moved `GameRules` to its own package). It backports what
  fabric-api >=5.x's `setConsistentSettings` does natively, and exists because
  1.21.9's spawn rework made CI's software-GL runners blow the harness's
  hardcoded 1200-tick world-load budget preparing the default 10-radius spawn
  area. An `LP_NUM_THREADS` cap was tried first on a CPU-starvation theory and
  refuted (identical 16% freeze capped and uncapped) - don't re-add it.
- Loom here is **1.17.491**: run-config edits need the Provider API
  (`programArguments.set(...)`), not FlightHud's older `programArgs` style.

Twelve eligible Fabric cells (1.21.4 through 26.2; older cells predate the API).
Six were run locally - 1.21.4, 1.21.8, 1.21.9, 1.21.11, 26.1, 26.2, chosen to span
every Stonecutter boundary in `OrientationKeyBind` - and all twelve run in CI under
`xvfb` in the `client-gametest` job. That job deliberately does not gate the release
steps; read its result rather than inferring it from a test-build existing.

Three negative controls were run against it and each gave its own correct diagnosis
(no-op tick listener / unreachable registration / `snapYaw` off by 45). If you change
the assertions, re-run controls like those - a green client gametest is very easy to
write by accident.

## Stonecutter Preprocessor - critical gotchas

The project uses Stonecutter for conditional compilation:

```java
//? if fabric {
import net.fabricmc.api.ClientModInitializer;
//?} elif neoforge {
/*import net.neoforged.fml.common.Mod;
*///?}
```

Two non-obvious mechanics that have caused real, hard-to-diagnose build
failures in this repo (full writeups in `PLAN.md`):

1. **The subproject matching `settings.gradle.kts`'s `vcsVersion` compiles
   directly from the raw checked-in source**, not from a generated copy.
   Every other subproject compiles a Stonecutter-generated copy under
   `versions/<id>/build/generated/stonecutter/main/java/...`. This means the
   raw file's literal (uncommented) branches must always exactly match
   whichever conditions are true for `vcsVersion` — get this wrong and only
   that one subproject breaks, invisibly to everything else.
2. **Never hand-edit the `stonecutter active "..."` marker** in
   `stonecutter.gradle.kts`. Use
   `./gradlew "Set active project to <mc>-<loader>"` instead - it also
   resyncs the raw source's comment state to match. Hand-editing the marker
   leaves the raw file's comments inconsistent with the new active version.
3. **Never place a plain `//` explanatory comment as the first line(s)
   inside a `//? if/elif { ... }` block's content region** if that block is
   ever disabled (`/* ... */`-wrapped) for some other target - Stonecutter's
   generator strips the leading `//` from such lines when re-enabling the
   block, turning documentation into broken bare Java text. Put explanatory
   comments *before* the `//? if` marker line instead (top-level, never
   touched by the generator).

## Verification bar: build green is necessary but not sufficient

A prior defect in this exact repo had every subproject reporting
`BUILD SUCCESSFUL` while every shipped jar was silently missing its entire
client-side mod code (a broken `sourceSets.main.java.srcDir(...)` wiring —
see `PLAN.md`). **Always spot-check jar contents** (`unzip -l <jar> | grep
Class`) across a representative sample after any structural change to the
source-set or build-script layout, not just the build task's exit status.

## Code Conventions

- Package namespace: `net.critical.orientation`
- Mod ID: `orientation`
- Shared, loader/version-agnostic logic stays in `OrientationCommon.java`
- Use Stonecutter `//? if loader`/`//? if >=version` for loader/version-specific code
- Tests target `OrientationCommon`'s pure logic (no loader mocking needed)

## Git / repository state

- Default branch is `main`, both locally and on GitHub (renamed from
  `master`). The repo was archived (read-only) earlier in its history; it has
  since been unarchived and pushes to `main` work normally. See `PLAN.md`
  "Repository state / outstanding blockers" for the confirmation history.
- `.github/workflows/` changes are pushable. The `bshuler` gh token gained the
  `workflow` OAuth scope on 2026-08-13; the older note here claiming GitHub
  rejects any workflow-file push is obsolete. (The archive-lock history above
  is a separate matter and also resolved.)
- Never publish to Modrinth/CurseForge from an agent session - build-only.

## Distribution

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/critical-orientation)
- [Modrinth](https://modrinth.com/mod/critical-orientation)
- GitHub releases
- Licensed under CC0-1.0 (public domain)
