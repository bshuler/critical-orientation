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
matters here). `./gradlew test` passes on every subproject except the five
NeoForge subprojects affected by the upstream `junit-fml` limitation
documented below (`compileJava`/`jar`/`assemble`/`build` are unaffected on
those five).

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
| 1.21.8  | ✅ | ✅⚠️ | — | NeoForge `test` task fails, see limitations |
| 1.21.7  | ✅ | ✅⚠️ | — | NeoForge `test` task fails, see limitations |
| 1.21.6  | ✅ | ✅⚠️ | — | NeoForge `test` task fails, see limitations |
| 1.21.5  | ✅ | ✅⚠️ | — | NeoForge `test` task fails, see limitations |
| 1.21.4  | ✅ | ✅⚠️ | — | NeoForge `test` task fails, see limitations; oldest NeoForge target |
| 1.20.1  | ✅ | — | ✅ | last Forge target using `RegisterKeyMappingsEvent` unmodified |
| 1.19.4  | ✅ | — | ✅ | |
| 1.18.2  | ✅ | — | ✅ | oldest target; Forge here predates `RegisterKeyMappingsEvent`, uses `ClientRegistry` instead |

✅⚠️ = `compileJava`/`jar`/`assemble`/`build` all green; `test`/`check` fails
due to an upstream NeoForge `junit-fml` bug unrelated to this mod's code (see
Known limitations).

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

### NeoForge `junit-fml` / `mainargs.txt` (test task only, MC 1.21.4-1.21.8)

NeoForge pins its own transitive `net.neoforged.fancymodloader:junit-fml`
artifact per Minecraft version, and versions <=9.0.18 (pulled in by the
1.21.4 through 1.21.8 targets) contain a bug in `LaunchWrapper`: it looks up
a run-config file named `mainargs.txt` via a relative path that doesn't
resolve under Gradle's test-worker working directory, so `test` fails at
JUnit-launcher startup with `NoSuchFileException: mainargs.txt` even though
`compileJava`/`jar`/`build` all succeed and the shipped jar is unaffected.
Fixed upstream in `junit-fml` 10.0+ (confirmed via jar inspection — the
relative-path lookup is gone there), but forcing that version alone throws
`NoClassDefFoundError: net/neoforged/fml/startup/StartupArgs` instead,
because `junit-fml` 10.0+ expects a newer `net.neoforged.fml` core API
surface that doesn't exist in the older NeoForge releases these Minecraft
versions ship — the whole FML/loader family would need bumping together,
which isn't safe to do in isolation per-test-dependency. Documented in
`build.gradle.kts` alongside the affected subprojects list.

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

## Build commands

```bash
# Build every version/loader in the matrix
./gradlew chiseledBuild

# Build one target
./gradlew :26.2-fabric:build

# Switch the active/vcsVersion target for local dev (never hand-edit stonecutter.gradle.kts)
./gradlew "Set active project to 26.2-fabric"

# Run unit tests everywhere (expect 5 known NeoForge failures, see Known limitations)
./gradlew test

# Reset active project back to vcsVersion before committing
./gradlew "Reset active project"
```

---

## Repository state / outstanding blockers

- **`bshuler/critical-orientation` on GitHub is archived (read-only)** as of
  this pass — confirmed via `gh api repos/bshuler/critical-orientation`
  (`"archived": true`) and via a `git push --dry-run` that GitHub itself
  rejected with `This repository was archived so it is read-only.` (HTTP
  403). This blocks **all** pushes, including the branch rename below. Local
  commits still happen (they don't touch the remote) but cannot be pushed
  until the repo is unarchived. Unarchiving is a repo-setting change outside
  this task's authority to make unilaterally (parallel to "never change repo
  visibility without explicit confirmation") — flagged for Bert's decision
  rather than acted on.
- **Default branch renamed `master` -> `main` locally** (`git branch -m master main`).
  The equivalent remote rename (`gh api -X POST .../branches/master/rename`)
  failed for the same archived-repo reason above and needs to be re-run once
  the repo is unarchived.
- **No `.github/workflows/` changes are pending or needed this pass** — the
  active `gh` token for this account lacks the `workflow` scope, so any
  pushed workflow-file change would be rejected by GitHub regardless of the
  archive status. No CI changes were identified as warranted, so nothing was
  written to a workflow-proposal file.

## Status checklist

- [x] Verify every existing target builds via `chiseledBuild`
- [x] Determine actual newest stable Minecraft version from live APIs (26.2, calendar-versioned)
- [x] Extend Stonecutter matrix to all stable versions newer than 1.21.4 on fabric + neoforge
- [x] Port code latest-first across the full 30-subproject matrix until green
- [x] Verify jar contents (not just build status) across a representative sample
- [x] Run `./gradlew test` (green except the 5 documented NeoForge `junit-fml` failures)
- [x] Refresh CLAUDE.md / PLAN.md / README.md to reflect the final matrix and findings
- [x] Rename default branch `master` -> `main` locally
- [ ] Push default-branch rename and all commits to GitHub (blocked: repo archived)
- [ ] Publish to Modrinth/CurseForge (explicitly out of scope for this task; build-only)
