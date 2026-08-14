# Critical Orientation - Modernization Plan

## Executive Summary

Critical Orientation is designated **THE HOUSE TEMPLATE** for the broader
program of keeping every one of Bert's Minecraft mods building on all modern
Minecraft versions and loaders, latest first. This document tracks the
modernization of this repo to that standard and records the porting notes,
Stonecutter mechanics, and known limitations discovered along the way so the
next mod modernized from this template doesn't have to rediscover them.

**Core strategic decisions:**
- **Multi-version**: Stonecutter preprocessor — single codebase, version-conditional code blocks.
- **Multi-loader**: Stonecraft (Stonecutter + Architectury Loom under one plugin) — Fabric, Forge, NeoForge.
- **Scope**: Client-side only (direction snapping for ice boat placement). No dedicated-server code path exists on any loader.
- **Source layout**: all loader/version-conditional code lives in the single `src/main/java` source set (see "Source set layout" below) — this is a deliberate, mod-specific choice, not a house-wide rule (see note at the end of this section).

## Status: matrix is green

All 30 subprojects (15 Minecraft versions x up to 2 loaders) build, and their
jars contain the correct client-side classes (verified by unzip, not just by
watching the build succeed — see "Known limitations" for why that distinction
matters here). `./gradlew test` passes on every subproject — the five NeoForge
subprojects (1.21.4-1.21.8) that used to fail on the upstream `junit-fml` bug
are fixed by a test-classpath exclusion (see Known limitations).

---

## Version x loader matrix

| Minecraft | Fabric | NeoForge | Forge | Notes |
|-----------|:------:|:--------:|:-----:|-------|
| 26.2   | ✅ | ✅ | — | newest stable at time of writing (calendar-versioned) |
| 26.1.2 | ✅ | ✅ | — | |
| 26.1.1 | ✅ | ✅ | — | |
| 26.1   | ✅ | ✅ | — | |
| 1.21.11 | ✅ | ✅ | — | `Identifier` replaces `ResourceLocation` from here on |
| 1.21.10 | ✅ | ✅ | — | |
| 1.21.9  | ✅ | ✅ | — | `KeyMapping.Category` replaces plain `String` from here on |
| 1.21.8  | ✅ | ✅ | — | NeoForge `test` needs the `junit-fml` exclusion, see limitations |
| 1.21.7  | ✅ | ✅ | — | NeoForge `test` needs the `junit-fml` exclusion, see limitations |
| 1.21.6  | ✅ | ✅ | — | NeoForge `test` needs the `junit-fml` exclusion, see limitations |
| 1.21.5  | ✅ | ✅ | — | NeoForge `test` needs the `junit-fml` exclusion, see limitations |
| 1.21.4  | ✅ | ✅ | — | NeoForge `test` needs the `junit-fml` exclusion, see limitations; oldest NeoForge target |
| 1.20.1  | ✅ | — | ✅ | last Forge target using `RegisterKeyMappingsEvent` unmodified |
| 1.19.4  | ✅ | — | ✅ | |
| 1.18.2  | ✅ | — | ✅ | oldest target; Forge here predates `RegisterKeyMappingsEvent`, uses `ClientRegistry` instead |

Fabric+NeoForge is used for every MC version from 1.20.5 onward that is old
enough to have both loaders available; Fabric+Forge is used for every version
≤1.20.4 (NeoForge did not exist before 1.20.2, and its early 1.20.2-1.20.4
builds are not targeted here — the matrix starts NeoForge coverage at 1.21.4).
Quilt is not built directly; see "Quilt compatibility" below.

Beta/pre-release caveats: 26.1, 26.1.1 and 1.21.9 shipped as NeoForge betas at
the versions used here (there was no stable NeoForge release for those
Minecraft snapshots/early releases at time of writing) — this is expected and
not a defect in this repo.

---

## Source set layout

This mod is 100% client-side on every loader (`fabric.mod.json` declares
`"environment": "client"`; `mods.toml`/`neoforge.mods.toml` declare
`side = "CLIENT"` on every dependency). There is no dedicated-server code
path to isolate `src/client/java` from, so all code — including the
loader-specific entry point (`OrientationClient.java`) and keybind
registration (`OrientationKeyBind.java`) — lives directly in
`src/main/java`. An earlier revision of this repo kept those two files under
`src/client/java` and added `sourceSets { main { java.srcDir("src/client/java") } }`
to `build.gradle.kts` to fold them in — that wiring was broken (see "Fixed:
client sourceSet not actually compiled" below) and has been replaced by
physically moving the files into `src/main/java`.

**This is a mod-specific choice, not a house-wide rule.** The sibling
`critical-flight-details` repo (also client-only) deliberately does *not*
split source sets at all, keeping everything in `src/main/java` from the
start — an equally valid approach for its shape of code. Neither approach
should be forced onto the other mod; each mod should pick whichever layout
its own code more naturally fits.

---

## Stonecutter mechanics (porting notes for future mods)

These are non-obvious things learned while making the matrix green, worth
knowing before touching this codebase (or any mod cloned from this template)
again.

### `vcsVersion` compiles directly from raw, un-generated source

`settings.gradle.kts`'s `shared { vcsVersion = "26.2-fabric" }` is not just
metadata — the subproject matching that id compiles **directly from the raw
checked-in `src/main/java` tree**, not from a Stonecutter-preprocessed copy.
Every *other* subproject goes through `stonecutterPrepare`/`stonecutterGenerate`,
which writes a per-version-and-loader-preprocessed copy into
`versions/<id>/build/generated/stonecutter/main/java/...` and compiles that
instead. Practical consequence: whichever conditional branches evaluate
`true` for the `vcsVersion` target must be the branches literally
uncommented in the raw file at all times — a stray double-uncommented (or
double-commented) branch breaks only the `vcsVersion` subproject's own build,
invisibly to every other subproject's (correctly-generated) build.

### Use the "Set active project to ..." task, never hand-edit the marker

`stonecutter.gradle.kts` contains a line like
`stonecutter active "26.2-fabric" /* [SC] DO NOT EDIT */` — the comment is
not decorative. Hand-editing that string does not resync the raw source
file's comment state to match, and can leave the raw file (see above) in a
broken, inconsistent state that only surfaces when that exact subproject is
built. `./gradlew tasks --all | grep -i "active project"` reveals the
sanctioned tasks:

- `"Set active project to <mc>-<loader>"` — switches the active version *and*
  reprocesses all versioned comments in the raw file to match. **Always use
  this**, never hand-edit the marker.
- `"Refresh active project"` — reprocesses comments for the currently active
  version without changing which version is active. Useful for fixing
  comments left in a wrong state.
- `"Reset active project"` — sets the active version back to the project's
  `vcsVersion` (`26.2-fabric` here). Its own description says to run this
  before committing — i.e. the convention is that `vcsVersion` should be the
  active state at commit time.

### Explanatory comments must live *outside* `//? if/elif` block markers

Stonecutter's comment-toggle generator, when it enables (uncomments) a
disabled `/* ... */`-wrapped block, strips a leading `//` from *every* plain
line-comment it finds positioned as the first content inside that block's
region — not just the `/*`/`*/` wrapper characters. Concretely, this raw
source (previously present in `OrientationKeyBind.java`) is broken:

```java
//?} elif forge {
// Forge < 1.19 has no RegisterKeyMappingsEvent; key bindings are
// registered directly via ClientRegistry instead.
/*import net.minecraftforge.client.ClientRegistry;
...
*///?}
```

When Stonecutter generated the tree for a target where `forge` (and `<1.19`)
evaluates true, it stripped the leading `//` from *all four* comment lines
too (not just the `/*`/`*/` markers around the imports), turning genuine
documentation into bare, invalid Java text and breaking
`:1.18.2-forge:compileJava` with `error: class, interface, enum, or record
expected`. Root cause: the explanatory comment lines sat between the
`elif forge {` marker and the block's `/*` opener rather than being wrapped
by it or placed entirely outside the block.

**Fix / rule going forward**: any explanatory comment meant to survive every
toggle state must be placed **before** the `//? if`/`elif` marker line
entirely (at the top level, never touched by the generator), exactly like
the "ClientTickEvent has always lived under..." and "KeyMapping's 4th
constructor argument changed..." comment blocks elsewhere in
`OrientationKeyBind.java`, both of which compile cleanly everywhere in the
matrix. Never place a plain `//` comment as the first line(s) *inside* a
block's content region if that block is ever also wrapped in `/* */` for
disabling — the generator does not distinguish "documentation" from "code"
inside that region.

---

## Version-boundary porting notes

Findings from porting this exact mod across 1.18.2 -> 26.2, useful reference
for any other mod ported from this template through the same span. All
confirmed by direct jar/bytecode inspection (`javap`, `unzip -l`), not
assumption.

| Boundary | Change | Source |
|---|---|---|
| Fabric `>=26.1` | `net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper` -> `net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper` (package + class rename) | jar inspection of Fabric API artifacts |
| MC `>=1.21.9` | `KeyMapping`'s 4th constructor argument changes from a plain `String` category to `KeyMapping.Category` (built via `KeyMapping.Category.register(<id>)`) | `javap` across 1.21.4-1.21.8 (String) vs 1.21.9+ (Category) |
| MC `>=1.21.11` | The id type passed to `Category.register`/used throughout is renamed `ResourceLocation` -> `Identifier` | jar inspection: 1.21.9/1.21.10 ship `ResourceLocation`, 1.21.11+ ship `Identifier` |
| Forge/NeoForge | `ClientTickEvent` has **always** lived at `net.neoforged.neoforge.client.event.ClientTickEvent` on NeoForge (never under `event.tick`, which only holds Entity/Level/Player/Server tick events) | jar inspection, NeoForge 21.4.157 through 26.2.0.58 |
| Forge `>=1.19` vs `<1.19` | `net.minecraftforge.client.event.RegisterKeyMappingsEvent` was introduced at Forge 1.19; MC 1.18.2's Forge (40.3.12) has no such class. Pre-1.19, key bindings are registered directly via `net.minecraftforge.client.ClientRegistry.registerKeyBinding(KeyMapping)` inside `register()`, with no separate event handler needed. | jar inspection: `forge-1.18.2-40.3.12-universal.jar` lacks the class; `forge-1.19.4-45.4.3-universal.jar` and later have it; `javap` on `ClientRegistry` confirms the direct static-call signature |
| `KeyMapping.wasPressed()` | **Does not exist** on `KeyMapping` in any version in this matrix — the actual edge-triggered "was this key just pressed" method is `consumeClick()`. This bug predated this modernization pass and was only caught because the file containing it had never actually been compiled before (see below). | `javap` on real `KeyMapping.class` from both 1.21.4's and 26.2's merged-mojang jars |
| GuiGraphics removal (26.2) | Confirmed **not applicable** — this mod never touches `GuiGraphics` anywhere in its code. | full read of `OrientationClient.java`/`OrientationKeyBind.java` |
| `Entity.getLevel()` vs `level()` (pre-1.20) | Confirmed **not applicable** — `OrientationCommon.java` is pure yaw math with no `Entity`/`Level` API usage at all. | full read of `OrientationCommon.java` |

---

## Fixed: client sourceSet not actually compiled (root cause of several latent bugs)

Before this pass, `src/client/java/net/critical/orientation/{OrientationClient,OrientationKeyBind}.java`
were wired into the build via `sourceSets { main { java.srcDir("src/client/java") } }`
in `build.gradle.kts`. Fabric Loom's `splitEnvironmentSourceSets()` API — the
correct way to add a genuine client-only source set on Fabric — is
Fabric/Quilt-Loom-only and has no equivalent on the Forge/NeoForge
subprojects generated by Stonecraft, so it couldn't be applied uniformly
across the matrix, and the ad-hoc `srcDir` addition above silently never
worked: `compileJava`/`jar`/`assemble` all reported success because they
simply never saw those files. **The entire matrix was "green" while every
shipped jar was missing its client-side mod code entirely** — a defect only
visible by inspecting jar contents, never by watching build status.

Fix: physically moved both files into `src/main/java` via `git mv` and
removed the broken `sourceSets` block (see "Source set layout" above for why
this is safe for a 100%-client mod).

Once the files were finally compiled for the first time, three previously
unnoticed and unrelated bugs surfaced immediately (all now fixed, and listed
in the version-boundary table above for the ones that are version-boundary
shaped): `wasPressed()` -> `consumeClick()`, NeoForge's `ClientTickEvent`
import package, and the Forge-1.18.2-lacks-`RegisterKeyMappingsEvent`
boundary. None of these were introduced by this modernization pass — they
were always broken and simply never compiled, which is the exact reason
"build green" was insufficient as a verification bar for this project and
jar-content verification (`unzip -l`) was added as a mandatory extra step.

---

## Known limitations

### NeoForge `junit-fml` / `mainargs.txt` (RESOLVED — was: test task failing, MC 1.21.4-1.21.8)

NeoForge pins its own transitive `net.neoforged.fancymodloader:junit-fml`
artifact per Minecraft version, and versions <=9.0.18 (pulled in by the
1.21.4 through 1.21.8 targets) contain a bug in `LaunchWrapper`: it looks up
a run-config file named `mainargs.txt` via a relative path that doesn't
resolve under Gradle's test-worker working directory, so `test` failed at
JUnit-launcher startup with `NoSuchFileException: mainargs.txt` even though
`compileJava`/`jar`/`build` all succeeded and the shipped jar was unaffected.
Fixed upstream in `junit-fml` 10.0+ (confirmed via jar inspection — the
relative-path lookup is gone there), but forcing that version alone throws
`NoClassDefFoundError: net/neoforged/fml/startup/StartupArgs` instead,
because `junit-fml` 10.0+ expects a newer `net.neoforged.fml` core API
surface that doesn't exist in the older NeoForge releases these Minecraft
versions ship.

**The actual fix** (proven first in the sibling simple-utilities-mod and
ToroHealth repos, then ported back here): `junit-fml` exists to bootstrap FML
for *gametests*. This repo's tests are plain pure-logic JUnit tests that need
none of that, so `junit-fml` — whose auto-registered
`LauncherSessionListener` is what performs the failing `mainargs.txt` lookup
— is simply excluded from `testRuntimeClasspath` on NeoForge subprojects in
`build.gradle.kts`. All 30 subprojects' `test`/`check` now pass.

**What this exclusion costs (recorded 2026-08-13).** It is the right call *for
this repo*, but it is not a universal one, and the wording above ("the actual
fix") undersells the trade-off. `junit-fml` is precisely NeoForge's own
*loaded-test bootstrap* — it is what stands FML up so a test can run against a
real, loaded game. NeoForge's supported loaded-test path (`neoForge { unitTest
{ enable(); testedMod = ... } }`, the `net.neoforged:testframework` artifact,
`@ExtendWith(EphemeralTestServerProvider.class)` injecting a live
`MinecraftServer`, and `gradlew runGameTestServer`) is **ModDevGradle-only**,
and this repo builds on Architectury Loom via Stonecraft, so that path is
unavailable here regardless of the exclusion. Excluding `junit-fml` therefore
costs nothing today — but if a cell is ever migrated to ModDevGradle, this
exclusion must be revisited before writing any loaded NeoForge test, because it
would silently disable the very bootstrap such a test depends on.

### Forge `pack.mcmeta` / `compileTestJava` task-graph ordering

Forge's `generatePackMCMetaJson` task writes into the main source set's
resources output without declaring that as a tracked task output, which
Gradle's task validation flags as an undeclared dependency from
`compileTestJava` (which consumes `sourceSets.main.output` as part of the
test compile classpath). Fixed in `build.gradle.kts` via
`tasks.matching { it.name == "compileTestJava" }.configureEach { dependsOn(tasks.matching { it.name == "generatePackMCMetaJson" }) }`,
a lazy filter that is a harmless no-op on loaders without that task (e.g.
Fabric).

### Forgix: investigated, rejected

Forgix (a tool that merges per-loader jars into one universal jar) was
investigated as a way to ship a single artifact instead of per-loader jars.
Conclusion: **architecturally incompatible** with this project's Stonecutter
+ Stonecraft setup — Forgix expects independently-built loader jars it can
splice together after the fact, not the single-multi-version-subprojects
model Stonecutter uses, and the version-conditional preprocessing Stonecutter
already performs would conflict with Forgix's own jar surgery. Decision:
ship per-loader jars, one per (version, loader) subproject, as the matrix
already does.

### Quilt compatibility (not built, documented instead)

Quilt is not built as a separate target in this matrix. Quilt is
binary-compatible with Fabric mods via its Fabric API compatibility layer,
and this mod uses no Fabric-Loom-specific or Quilt-incompatible APIs, so the
Fabric jars produced by this matrix are expected to work unmodified under
Quilt. This is a documentation-only statement, not a tested/verified claim —
no Quilt-specific subproject or CI job exists.

---

## Test coverage (Phase 2)

### How it's wired

JUnit 5 was already present from phase 1 (`testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")`
+ `useJUnitPlatform()` in the central `build.gradle.kts`, which Stonecutter's `centralScript`
mechanism uses as the literal build file for every subproject — no per-version `build.gradle.kts`
files exist). Phase 2 added the `jacoco` core Gradle plugin to that same central script:

```kotlin
plugins {
    id("gg.meza.stonecraft")
    jacoco
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

val jacocoExcludes = listOf(
    "net/critical/orientation/OrientationClient.class",
    "net/critical/orientation/OrientationClient$*.class",
    "net/critical/orientation/OrientationKeyBind.class",
    "net/critical/orientation/OrientationKeyBind$*.class",
)

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports { xml.required.set(true); html.required.set(true) }
    classDirectories.setFrom(classDirectories.files.map { fileTree(it) { exclude(jacocoExcludes) } })
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    classDirectories.setFrom(classDirectories.files.map { fileTree(it) { exclude(jacocoExcludes) } })
    violationRules {
        rule { limit { counter = "LINE"; value = "COVEREDRATIO"; minimum = "1.00".toBigDecimal() } }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
```

Because this one script is the build file for all 30 subprojects, this wiring applies matrix-wide;
since the `junit-fml` test-classpath exclusion landed (see Known limitations), `./gradlew
chiseledBuild` runs `test`/`check` green on every cell, and the full matrix is part of the
verification loop.

Run it with: `./gradlew test jacocoTestReport jacocoTestCoverageVerification` (or just `./gradlew
check` / `./gradlew build`, since `check` now depends on the verification task). Reports land at
`versions/26.2-fabric/build/reports/jacoco/test/html/index.html` (HTML) and
`.../jacocoTestReport.xml` (XML).

### Scope and result

Only `OrientationCommon` (pure, loader/version-agnostic yaw math) is genuinely testable headless.
It is now at **100% line and branch coverage** (verified via the XML report's counters: `LINE
missed="0" covered="19"`, `BRANCH missed="0" covered="44"`, `CLASS missed="0" covered="1"`) with 4
of its 5 members covered by real assertions and the 5th (see below) deliberately excluded rather
than gamed.

Excluded from the 100% bar, with reasons:

- **`OrientationClient`** — the loader entry point. Fabric implements `ClientModInitializer`;
  NeoForge/Forge are `@Mod`-annotated and hook `FMLClientSetupEvent` via the mod event bus. All
  three variants require a real loader/game environment to construct or invoke. Untestable
  headless.
- **`OrientationKeyBind`** — keybind registration and tick handling. Its static initializer calls
  `KeyMapping.Category.register(...)` against real Minecraft registry classes, and its methods use
  `KeyMapping`, `Minecraft`, `GLFW`, and loader tick-event types. Even its two trivial
  `normalizeHeadYaw`/`roundYaw` delegate wrappers are unsafe to invoke in a headless unit test,
  because merely *referencing* the class triggers that static initializer. Untestable headless.
- **`OrientationCommon`'s private no-op constructor** — not excluded via `classDirectories`
  (that would exclude the whole class) but via JaCoCo's built-in `GeneratedFilter`: any annotation
  whose *simple name* is `Generated` (regardless of package) causes JaCoCo to skip that member from
  coverage entirely, both numerator and denominator. `OrientationCommon` was changed to `final`
  with an explicit `private` constructor (idiomatic for a stateless static-only utility class,
  and a minor real hardening — previously anyone could pointlessly `new OrientationCommon()`) that
  throws `UnsupportedOperationException`, annotated `@Generated`. A tiny project-local
  `net.critical.orientation.Generated` marker annotation was added (source at
  `src/main/java/net/critical/orientation/Generated.java`) instead of depending on
  `javax.annotation.Generated`, which modern JDKs (9+) removed from the default module path. This
  constructor is deliberately **not** exercised via reflection to flip its coverage bit, per the
  no-reflection-hacks rule — it is real, honestly-documented dead code by design.

### Bugs / gaps found while writing tests

No functional bugs — but writing tests to reach 100% (rather than stopping once "the obvious cases"
passed) exposed two real gaps in phase 1's test suite that a coverage bar alone wouldn't have
caught without inspecting *which* lines/branches were hit:

1. **`normalizeHeadYaw`'s `>180`/`<-180` post-modulo adjustment branches were never exercised.**
   Phase 1's overflow tests (`450`, `540`, `720`, `810`, and their negatives) all happen to be
   multiples that divide down to values already inside `[-180, 180]` after Java's `%` operator
   (e.g. `450 % 360 = 90`), so the `yaw -= 360` / `yaw += 360` lines were dead from the test suite's
   perspective despite `roundYaw` and the rest of `normalizeHeadYaw` being well covered. Added
   `testPostModuloAbove180`/`testPostModuloBelowNegative180` with inputs like `270`, `200`, `-270`,
   `-200` that land outside `[-180, 180]` *after* the modulo but before the adjustment, which do hit
   those branches.
2. **`roundYaw`'s final `return yaw;` fallback (reached only when none of its ten explicit
   `[-180, 180]` sub-range checks match) was untested.** It's a genuine defensive path: `roundYaw`
   is public and documented as expecting pre-normalized input, but nothing stops a caller from
   invoking it directly with an out-of-range value. Added `testOutOfRangeFallsThrough` calling
   `roundYaw` directly (bypassing `normalizeHeadYaw`) with `200`, `-200`, `181` to exercise that
   passthrough behavior.
3. Also added direct tests for `snapYaw` (previously only exercised indirectly through
   `normalizeHeadYaw`/`roundYaw` calls, never called itself) and `init()` (a documented no-op,
   asserted to not throw).

None of the gotchas in the phase-2 brief (`u`, `v`, `w`) apply to this repo: there's no MockBukkit
(not a Bukkit plugin), no `compileOnly`-typed field needing a Mockito workaround (no mocks are used
at all — `OrientationCommon` has no dependencies to mock), and no `try`/`catch` blocks in the tested
code. Gotcha `t` (verify the analyzed class count, not just a green check) does apply: Gradle's
built-in `jacoco` plugin doesn't emit Maven-jacoco-plugin's "Analyzed bundle ... with N classes"
log line, so this was verified instead by inspecting the XML report directly — exactly one real,
coverable class (`OrientationCommon`) is present with `CLASS missed="0" covered="1"`; the
`Generated` marker annotation also appears as an empty `<class .../>` entry (annotation types
compile to a class file but contain no executable bytecode) and correctly contributes nothing to
any counter.

---

## Folia

**N/A — client mod.** Folia is a server-side Paper fork; this mod has no server component, no
Bukkit scheduler usage, and no plugin.yml. Nothing to evaluate.

---

## Build commands

```bash
# Build every version/loader in the matrix
./gradlew chiseledBuild

# Build one target
./gradlew :26.2-fabric:build

# Switch the active/vcsVersion target for local dev (never hand-edit stonecutter.gradle.kts)
./gradlew "Set active project to 26.2-fabric"

# Run unit tests everywhere (all green — the historical NeoForge junit-fml failures are fixed, see Known limitations)
./gradlew test

# Reset active project back to vcsVersion before committing
./gradlew "Reset active project"
```

---

## Repository state / outstanding blockers

- **RESOLVED**: `bshuler/critical-orientation` was archived (read-only) at
  the start of the phase-1 pass (confirmed at the time via
  `gh api repos/bshuler/critical-orientation` returning `"archived": true`
  and a rejected push). The repo has since been unarchived and `main` set as
  the default branch (confirmed via `gh api repos/bshuler/critical-orientation
  --jq '{archived, default_branch}'` -> `{"archived": false, "default_branch":
  "main"}`); phase-1 commits `bc63ff3`/`aa77bbc`/`58da9a8` are pushed and
  `main` is up to date with `origin/main`. Pushing directly to `main` now
  works normally.
- **Default branch renamed `master` -> `main`**, both locally and on the
  remote (superseded the earlier blocked-remote-rename state above).
- **No `.github/workflows/` changes are pending or needed this pass** — none
  were identified as warranted, so nothing was written to a workflow-proposal
  file. Note the token constraint that originally motivated this bullet is
  gone: the `bshuler` gh token gained the `workflow` OAuth scope on 2026-08-13,
  so workflow-file changes are pushable if a future pass wants them.

## Status checklist

- [x] Verify every existing target builds via `chiseledBuild`
- [x] Determine actual newest stable Minecraft version from live APIs (26.2, calendar-versioned)
- [x] Extend Stonecutter matrix to all stable versions newer than 1.21.4 on fabric + neoforge
- [x] Port code latest-first across the full 30-subproject matrix until green
- [x] Verify jar contents (not just build status) across a representative sample
- [x] Run `./gradlew test` (green on all 30 subprojects after the `junit-fml` test-classpath exclusion)
- [x] Refresh CLAUDE.md / PLAN.md / README.md to reflect the final matrix and findings
- [x] Rename default branch `master` -> `main` locally
- [x] Push default-branch rename and all commits to GitHub (repo unarchived; see "Repository state")
- [ ] Publish to Modrinth/CurseForge (explicitly out of scope for this task; build-only)

### Phase 2 (test coverage + Folia)

- [x] Wire JUnit 5 + JaCoCo into the Stonecutter central build script, gating `check`
- [x] Drive `OrientationCommon` (the only genuinely testable, pure-logic class) to 100% line/branch coverage
- [x] Document JaCoCo exclusions for the two Minecraft-runtime-touching classes
- [x] Folia verdict recorded (n/a - client mod)
- [x] Verify jar contents after adding a new compiled class (`Generated.class`)
- [ ] Run `chiseledBuild` as an end-of-pass regression check (see "Build sanity")

## Coverage in context (measured 2026-08-13)

Read from the JaCoCo XML report, not from whether the gate passes:

- **Analysed surface:** 2 of 4 compiled classes (50%).
- **Line coverage of that surface:** 100.0% (19 lines analysed).
- Classes outside that surface are excluded by the documented exclusion list. They
  are not covered by any test and are not runtime-verified.
  Measured from `critical-orientation/versions/1.18.2-fabric/build/reports/jacoco/test/jacocoTestReport.xml`.

A passing `check` means "no regression inside the analysed surface" — it does not
mean the whole codebase is tested to that percentage.

## Tier 1: loaded-game testing (added 2026-08-13)

The coverage numbers above measure *headless* tests against pure logic. They say
nothing about whether the mod's assumptions still hold inside a running game -
which, until now, nothing in this repo verified at all.

`net.fabricmc:fabric-loader-junit` closes that gap on the Fabric cells. It stands a
real Fabric loader up inside the JUnit worker, so a test can call
`SharedConstants.tryDetectVersion()` + `Bootstrap.bootStrap()` and then assert
against genuinely loaded Minecraft data. `LoadedGameTest` uses it to check the
snap math against vanilla's own `Direction.fromYRot` mapping rather than against
the test's own idea of where north is.

**Verified, not assumed:** `./gradlew test` runs it in all 15 Fabric cells
(1.18.2 -> 26.2) with 3 tests each, 0 failures, 0 errors, parsed from
`versions/*/build/test-results/test/*LoadedGameTest.xml`. Bootstrap costs 3-7s per
cell; the full-matrix `test` task still finishes in about 70s.

**What it does not cover.** This is a loaded *game*, not a loaded *client*: there
is no window, no render pass, no player entity. Keybind registration and the
client entry point remain untested and excluded, as documented above.

This paragraph used to end: *"Fabric's client gametest harness could cover those,
but this mod's client surface is a single keybind registration - the harness would
cost more to maintain than the line it protects."* **That was wrong, and it is
worth saying why rather than quietly deleting it.** The reasoning mistook line
count for risk. "A single keybind registration" is the entire mod from the user's
point of view - it is the only thing the mod does - and it is the one line whose
failure is invisible to every other tier in this repo. The harness turned out to
cost one source file and one manifest. See Tier 3 below.

**NeoForge cells have no equivalent.** Not an oversight: NeoForge's loaded-test
path is ModDevGradle-only. See the junit-fml note under Known limitations.

## Tier 3: client gametest (added 2026-08-14)

`src/gametest/java/net/critical/orientation/gametest/OrientationClientGameTest.java`
runs the mod inside a **real Minecraft client** - real window, real GL context,
real world, a real player - and presses a real backslash key through vanilla's
real keyboard handler.

```bash
unset JAVA_HOME && ./gradlew :26.2-fabric:runClientGameTest    # opens a real window
unset JAVA_HOME && ./gradlew :1.21.4-fabric:runClientGameTest
```

### Why this tier matters more here than in the sibling repos

The headless suite holds `OrientationCommon` at 100% line coverage, and Tier 1
checks its arithmetic against vanilla's own `Direction.fromYRot`. Neither touches
`OrientationKeyBind`, which is where the mod actually lives: the `KeyMapping`
construction, the per-loader registration call, the client-tick subscription, the
`consumeClick()` drain, and the three rotation writes. It is on the JaCoCo
exclusion list for a real reason - loading the class runs a static initialiser
against live registry classes - and Tier 1 cannot reach it either, having no
window, no input pipeline, no tick loop and no player.

The consequence, stated plainly: **delete the `ClientTickEvents.END_CLIENT_TICK`
registration, or the `KeyMappingHelper` call, and this repo stays entirely green.**
Thirty cells compile, thirty jars package, every headless and loaded test passes,
and the mod does nothing when you press the key. Before this file, nothing in the
repo could tell that build from a working one.

### What it asserts, and in what order

Each step guards the next from passing vacuously:

1. **The mod is loaded** (`FabricLoader.isModLoaded`).
2. **The keybind reached vanilla.** `key.orientation.snap` is present in the live
   `Minecraft.options.keyMappings` array and its `saveString()` is
   `key.keyboard.backslash`. Fabric's helper registers by appending to exactly
   that array and vanilla rebuilds its key-to-mapping lookup from it, so absence
   there *is* registration failure. Checked before the world opens so that
   "never registered" and "registered but never ticks" stay distinguishable -
   from the far end both look like a yaw that did not move.
3. **Nothing snaps on its own.** Yaw 12.3, one second of real ticks, no key
   pressed, must still read 12.3. Without this control every assertion below
   would also pass for a mod that snapped unconditionally every tick.
4. **The real keystroke snaps the view.** Fourteen starting yaws, one raw
   backslash press each, asserted on the live player's `getYRot()`,
   `getYHeadRot()` *and* `yBodyRot`.
5. **The drain loop is idempotent.** Two presses inside one tick, so
   `while (consumeClick())` runs twice in a single pass.

### The key is pressed raw, not via the mod's own KeyMapping

`TestInput` offers `pressKey(KeyMapping)`, which would be shorter and is wrong for
this test. Handing the harness the mod's own mapping object proves the object
exists; pressing `GLFW_KEY_BACKSLASH` as a bare keysym is what a *user* does, and
it reaches the mapping only by travelling the whole real path -
`KeyboardHandler.keyPress` -> `KeyMapping.click(key)` -> the static key-to-mapping
map vanilla rebuilds from `Options.keyMappings`. A mapping constructed but never
registered is simply not in that map and no click lands on it. That is the
property worth testing, and it is why assertion 2 exists separately: so a
*rebinding* regression fails with "bound to X, expected backslash" instead of a
mysterious non-snap.

Verified by bytecode, not assumed: `pressKey(InputConstants.Key)` reaches
`KeyboardHandlerAccessor.invokeKeyPress(window.handle(), action, ...)` on every API
version in the matrix.

### Case table

Chosen for the bucket edges and the wrap branches, not for round numbers:

| Start | Expect | Why this one |
|---|---|---|
| 22.5, 67.5 | 45, 90 | inclusive lower edges - an off-by-one comparison drops them a bucket |
| 44, 67.4, 100, 134.9, 179 | 45, 45, 90, 135, 180 | interior of each bucket |
| -100 | -90 | negative half |
| -157.5 | **180** | same heading as -180, different float; the mod writes the positive one |
| 350, 730, -395 | 0, 0, -45 | outside [-180,180] entirely - vanilla does not normalise on `setYRot`, so these drive `normalizeHeadYaw` against a genuinely denormalised *live* player rather than a hand-fed double |
| 0, 12.3 | 0, 0 | trivial identity, and the smallest real correction |

All three rotation fields are checked, not just `yRot`: the mod writes head and
body too, because writing only the camera leaves them visibly lagging, and an
assertion on `yRot` alone would not notice if those two writes were dropped.

### Staging: one command, and it is enforced

The sibling repos open with six `gamerule` commands. On 26.2 all six silently
failed for a full run - `TestServerContext#runCommand` swallows command failures
(bytecode-verified), and 26.2 had renamed every rule to snake_case and changed
three outright. Nothing caught it because nothing depended on it.

The lesson taken here is not "port the rename", it is **do not stage what the test
does not need**. A yaw does not care about daylight, weather, mobs or fire. So the
staging is `gamemode creative @p` and a landmark `fill`, and *both* are confirmed
by their observable effect rather than by having been issued:

- creative mode, by waiting on `client.player.isCreative()`;
- the landmark, by waiting until the **client** reports `GOLD_BLOCK` at the
  expected position - which also disposes of the chunk-sync race a server-side
  check would leave open.

`runCommand` also dispatches from the world spawn rather than the player, so the
fill is wrapped in `execute as @p at @s run`.

### The screenshots, and why the first version of them was worthless

The first passing run produced four screenshots of a featureless grass plain under
a clear sky. A consistent-settings world has no landmarks, so "before snap" and
"after snap" were pixel-for-pixel indistinguishable no matter how far the view had
turned. That is worse than no artifact: a reviewer flips through them and comes
away feeling informed.

So the staging now builds a six-block gold pillar ten blocks **due west** - west
*is* yaw 90, one of the eight snap targets. The pair is shot at 67.5 degrees and
then at 90: the pillar goes from well off to the right to dead centre under the
crosshair. Confirmed by eye on both 1.21.4 and 26.2, identical framing.

The numeric assertions remain the real evidence in this tier - unlike ToroHealth,
where the pixel count *is* the assertion. The screenshots here are for the human.

### Negative controls - run, not asserted

Three, each producing its own distinct and correct diagnosis:

| Injected defect | Failure message |
|---|---|
| tick listener registered as a no-op lambda | *"pressing backslash at yaw 12.3000 left the player at yaw 12.3000 ... The yaw did not move at all, so the keystroke never reached OrientationKeyBind.onClientTick - the mapping is registered (that was asserted before the world opened), so suspect the client-tick listener registration or the consumeClick() drain."* |
| `KeyBindingHelper.registerKeyBinding` made unreachable | *"'key.orientation.snap' is not in the live client's Options.keyMappings array ... The client did register 34 mappings: [key.attack, key.use, ...]"* |
| `snapYaw` returning its result + 45 | *"pressing backslash at yaw 0.0000 left the player at yaw 45.0000 ... The yaw did move, so the keystroke was received and the snap ran - this is OrientationCommon's arithmetic disagreeing with the pure suite"* |

Each was reverted and re-run green. Note the third: it failed on the `{0 -> 0}`
case, which looks like a freebie that no broken mod could fail. It is not - it is
the only case that catches an *additive* error at the origin.

### Coverage: twelve cells, one un-preprocessed file

`fabric-client-gametest-api-v1` first appears around fabric-api 0.106 / MC 1.21.2,
so the twelve Fabric cells from 1.21.4 to 26.2 are eligible - spanning API v4.1.1
through v6.0.0. The three older Fabric cells predate the API and no Forge/NeoForge
cell has an equivalent reachable from Architectury Loom (see Tier 4 under Known
limitations).

`ClientGameTestContext`, `FabricClientGameTest`, `TestWorldBuilder`,
`TestServerContext` and `TestInput` were checked with `javap` against every API jar
in the matrix and are method-identical across all of them. The one interface that
did change is `TestSingleplayerContext`, whose `getClientWorld()` became
`getClientLevel()` at v5 and `getConnection()` at v6 - none of the three is used
here. So the file carries **no Stonecutter branch at all**, despite the production
code it exercises crossing three boundaries (`KeyBindingHelper` ->
`KeyMappingHelper` at 26.1, `String` -> `KeyMapping.Category` at 1.21.9,
`ResourceLocation` -> `Identifier` at 1.21.11).

**Run locally and verified green** on six cells chosen to span every one of those
boundaries: 1.21.4 (API 4.1.1, `String` category, `KeyBindingHelper`), 1.21.8,
1.21.9 (`Category` + `ResourceLocation`), 1.21.11 (`Identifier`), 26.1
(`KeyMappingHelper`), 26.2 (API 6.0.0). Each console was read for swallowed
command failures, not just its exit code. **The other six (1.21.5, 1.21.6, 1.21.7,
1.21.10, 26.1.1, 26.1.2) compile locally but have only been run in CI** - stated
here rather than left to be assumed from the twelve-cell matrix.

### Wiring notes

Two corrections to Loom's generated run config live in `build.gradle.kts`, both
with the reasoning inline:

- Stonecraft passes `--username=developer` and Loom's gametest config passes its
  own, and joptsimple throws `MultipleArgumentsForOptionException` on the
  duplicate.
- Stonecraft's `setRunDir` would leave Loom's `deleteGameTestRunDir` `Delete` task
  pointed at the developer's repo-root `../../run` directory.

This repo uses Loom **1.17.491**, which needs the Provider API
(`programArguments.set(...)`, `runDirectory.set(...)`). The older 1.14.476 in
FlightHud uses `programArgs`/`runDir`, and on 1.17 `programArgs.removeAll {}`
throws `UnsupportedOperationException` from `ImmutableList.set`.

`src/gametest/resources/fabric.mod.json` is required and easy to forget. Without
it the client boots to the title screen and exits with **BUILD SUCCESSFUL** having
run no test at all - which is exactly what the first attempt here did.

### CI

`.github/workflows/build.yml` gains a `client-gametest` job: all twelve eligible
cells under `xvfb-run` with `LIBGL_ALWAYS_SOFTWARE=true`, uploading screenshots and
logs `if: always()`. It is a separate job from `build` and deliberately does **not**
gate the test-build release - a graphics-stack flake on a headless runner should
not withhold jars that compiled and passed every headless test. Read the job
result; do not infer it from the release existing.

All twelve run rather than a sample of the ends: a registration that stops taking
on exactly one version is precisely what this tier exists to catch.
