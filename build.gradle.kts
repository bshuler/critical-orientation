import gg.meza.stonecraft.mod

plugins {
    id("gg.meza.stonecraft")
    jacoco
}

// Stonecraft's built-in ProcessResources expansion only provides id/name/group/description/version/
// minecraftVersion/packVersion/fabricVersion/forgeVersion/neoforgeVersion (see Stonecraft's
// ProcessResources.kt). Loader/version *ranges* and the Java requirement aren't provided out of the
// box, so we compute real values here and expose them via variableReplacements rather than leaving
// unexpanded placeholders in the shipped fabric.mod.json / mods.toml / neoforge.mods.toml.
val minecraftVersionRange = "[${stonecutter.current.version},)"
// Fabric Loader's own dependency matcher does NOT understand Maven/OSGi bracket ranges like
// "[26.1.2,)" - despite that syntax working correctly for Forge/NeoForge's mods.toml. Fabric uses
// its own SemVer-style comparator strings (">=", "<", "~", "^", etc: see Fabric Loader's
// VersionPredicate). Using the Maven range in fabric.mod.json caused Fabric Loader to reject the
// exact running version at launch ("requires version [26.1.2,) ... but only the wrong version is
// present: 26.1.2!"), so Fabric gets its own equivalent open-ended range in Fabric's own syntax.
val fabricMinecraftVersionRange = ">=${stonecutter.current.version}"
val forgeVersionRange = "[${mod.prop("forge_version", "0")},)"
val neoforgeVersionRange = "[${mod.prop("neoforge_version", "0")},)"
// Any FML/NeoForge loader release works for this mod; we don't gate on a specific loader build.
val loaderVersionRange = "[0,)"
val javaVersion = if (stonecutter.eval(stonecutter.current.version, ">=21.6")) {
    25
} else if (stonecutter.eval(stonecutter.current.version, ">=1.20.6")) {
    21
} else {
    17
}

modSettings {
    clientOptions {
        fov = 90
        guiScale = 2
        narrator = false
        darkBackground = true
        musicVolume = 0.0
    }

    variableReplacements.put("minecraftVersionRange", minecraftVersionRange)
    variableReplacements.put("fabricMinecraftVersionRange", fabricMinecraftVersionRange)
    variableReplacements.put("forgeVersionRange", forgeVersionRange)
    variableReplacements.put("neoforgeVersionRange", neoforgeVersionRange)
    variableReplacements.put("loaderVersionRange", loaderVersionRange)
    variableReplacements.put("javaVersion", javaVersion)
}

dependencies {
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Tier 1 "loaded game" testing (Fabric cells only). fabric-loader-junit
    // stands a real Fabric loader up inside the JUnit worker, which is what
    // makes it legal to call SharedConstants.tryDetectVersion() +
    // Bootstrap.bootStrap() from a test and then assert against genuinely
    // loaded Minecraft data - real registries, real Direction math - instead
    // of mocks. Costs ~3.5s of bootstrap on the first test class.
    //
    // Fabric-only on purpose: NeoForge's equivalent bootstrap is junit-fml,
    // and its supported loaded-test harness (neoForge { unitTest { ... } } +
    // net.neoforged:testframework) is ModDevGradle-only, unavailable under
    // Architectury Loom - see the junit-fml exclusion comment further down.
    if (mod.isFabric) {
        testImplementation("net.fabricmc:fabric-loader-junit:0.19.3")
    }
}

// ---------------------------------------------------------------------------
// Tier 3: client gametests (a real Minecraft client, real GL context, real world)
// ---------------------------------------------------------------------------
//
// This is the only tier that can answer the question this mod exists to answer:
// when a player presses the key, does the view actually snap? Everything between
// the keystroke and the yaw - KeyMapping construction, the per-loader registration
// call, the client-tick listener, consumeClick() - lives in OrientationKeyBind,
// which is on the JaCoCo exclusion list because merely loading the class runs a
// static initializer against real Minecraft registry classes. Tier 1
// (fabric-loader-junit) bootstraps game data but has no window, no input, no tick
// loop and no player, so it cannot touch any of it either. The consequence is
// stark: delete the ClientTickEvents.END_CLIENT_TICK registration, or the
// KeyMappingHelper call, and every other tier in this repo stays green while the
// mod does nothing at all.
//
// Gated `>=1.21.4` because fabric-client-gametest-api-v1 first appears around
// fabric-api 0.106 / MC 1.21.2, so the 1.20.1/1.19.4/1.18.2 Fabric cells cannot
// run it; and `isFabric` because NeoForge's and Forge's equivalents need
// ModDevGradle, which this repo does not use (same underlying limitation as the
// junit-fml note below). That leaves twelve eligible cells, 1.21.4 through 26.2,
// spanning API v4.1.1 to v6.0.0. The test source uses only the surface those
// versions have in common, verified with javap against every one of the API jars
// in this matrix: ClientGameTestContext, FabricClientGameTest, TestWorldBuilder
// and TestInput are method-identical across all of them, and the one interface
// that did change - TestSingleplayerContext, whose getClientWorld() became
// getClientLevel() at v5 and getConnection() at v6 - is not used here. So one
// un-preprocessed file compiles on all twelve.
val clientGameTestSupported = mod.isFabric &&
    stonecutter.eval(stonecutter.current.version, ">=1.21.4")

if (clientGameTestSupported) {
    // Loom is not applied through this script's `plugins {}` block, so there is no
    // generated `fabricApi { }` accessor; configure the extension by type.
    extensions.configure<net.fabricmc.loom.api.fabricapi.FabricApiExtension>("fabricApi") {
        configureTests {
            createSourceSet.set(true)
            modId.set("orientation-gametest")
            // enableGameTests would wire `check` to dependsOn(runGameTest), dragging a
            // real *server* launch into every `./gradlew check`. This is a client-only
            // keybind mod with no server gametests, so it stays off.
            enableGameTests.set(false)
            enableClientGameTests.set(true)
            // eula deliberately left at its default (false): that is Mojang's EULA and
            // is not accepted automatically here. Only the dedicated-server variant
            // needs it.
        }
    }

    // Loom's `mods` container starts empty and createSourceSet adds an entry for the
    // gametest mod, which would otherwise be the *only* classpath group - leaving the
    // mod under test ungrouped in the dev run. Register it explicitly. Unlike the
    // sibling EasierVillagerTrading this repo does not call splitEnvironmentSourceSets(),
    // so there is exactly one source set to register and the generated `gametest`
    // source set - which Loom wires against `main` - already sees every mod class.
    extensions.configure<net.fabricmc.loom.api.LoomGradleExtensionAPI>("loom") {
        mods.create(mod.prop("id", "orientation")).sourceSet(sourceSets["main"])
    }

    // Stonecraft configures `loom.runs.all { }`, and because that is an `all` hook it
    // also catches the `clientGameTest` config Loom creates later. Two collisions
    // result, both corrected here for that one run config only:
    //
    // 1. Stonecraft appends `--username=developer` to every client-environment run
    //    while Loom's configureTests passes `--username Player0`. Minecraft's
    //    joptsimple parser treats `username` as single-valued, so the client dies with
    //    MultipleArgumentsForOptionException before the first frame. Loom's value is
    //    kept: this is not a human's dev session. `runClient` still logs in as
    //    `developer`.
    //
    // 2. The same hook calls setRunDir on every config, overwriting the
    //    `build/run/clientGameTest` Loom had just set with the repo-root `../../run` -
    //    the developer's own dev directory. That is destructive, not untidy:
    //    GameTestSettings' clearRunDirectory conventions to `true` and Loom registers a
    //    `deleteGameTestRunDir` Delete task over runConfig.getRunDir(), so launching
    //    the gametest would wipe the developer's dev world, options and screenshots.
    //    Restoring Loom's value also keeps the run dir per-cell, so twelve cells do not
    //    overwrite each other's screenshots. The Delete task reads getRunDir() lazily
    //    at realization, so correcting it here redirects the deletion too.
    //
    // afterEvaluate because Loom creates the run config while this script body is still
    // executing, and Stonecraft's `all` action fires at creation time.
    afterEvaluate {
        extensions.configure<net.fabricmc.loom.api.LoomGradleExtensionAPI>("loom") {
            runs.named("clientGameTest") {
                programArguments.set(
                    programArguments.get().filterNot { it.startsWith("--username=") }
                )
                runDirectory.set(layout.buildDirectory.dir("run/clientGameTest"))
            }
        }
    }

    // No explicit fabric-client-gametest-api-v1 dependency: Loom's configureTests adds
    // none, but Stonecraft already puts the whole fabric-api bundle on the compile
    // classpath and that bundle depends on every module including this one, so every
    // cell resolves it transitively. Declaring it again would risk pinning a second
    // version.

    // Nothing in the normal build graph compiles a `gametest` source set, so a compile
    // error there would sit undetected until someone launched the game. Compiling it is
    // cheap and needs no display; make `check` do it. Running it stays opt-in via
    // runClientGameTest.
    tasks.named("check") {
        dependsOn(tasks.named("compileGametestJava"))
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

// JaCoCo scope: OrientationClient (loader entry point) and OrientationKeyBind (keybind
// registration/tick handling) both touch real Minecraft/loader classes (ClientModInitializer,
// KeyMapping, GLFW, Minecraft, and loader lifecycle events) at class-load or call time - even
// OrientationKeyBind's tiny normalizeHeadYaw/roundYaw delegate methods are unsafe to invoke
// headless because merely referencing the class runs its static initializer, which calls
// KeyMapping.Category.register(...) against real Minecraft registry classes. Both are excluded
// here and documented in PLAN.md; only OrientationCommon (pure, loader-agnostic yaw math) is
// in scope for the 100% line-coverage bar.
val jacocoExcludes = listOf(
    "net/critical/orientation/OrientationClient.class",
    "net/critical/orientation/OrientationClient$*.class",
    "net/critical/orientation/OrientationKeyBind.class",
    "net/critical/orientation/OrientationKeyBind$*.class",
)

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(classDirectories.files.map { fileTree(it) { exclude(jacocoExcludes) } })
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    classDirectories.setFrom(classDirectories.files.map { fileTree(it) { exclude(jacocoExcludes) } })
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "1.00".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

// NeoForge pins its own transitive net.neoforged.fancymodloader:junit-fml artifact per Minecraft
// version, and versions <= 9.0.18 (pulled in by our 1.21.4 through 1.21.8 targets) contain a bug in
// LaunchWrapper: it looks up a run-config file named "mainargs.txt" via a relative path that doesn't
// resolve under Gradle's test-worker working directory, so `test` fails at JUnit-launcher startup
// with "NoSuchFileException: mainargs.txt". Forcing the upstream-fixed junit-fml 10.0+ doesn't work
// either (NoClassDefFoundError: net/neoforged/fml/startup/StartupArgs - it needs a newer FML core
// than these NeoForge releases ship). The actual fix: junit-fml exists to bootstrap FML for
// *gametests*; our tests are plain pure-logic JUnit tests that need none of that, so junit-fml's
// auto-registered LauncherSessionListener can simply be excluded from the test runtime classpath.
// Same fix as the sibling simple-utilities-mod / ToroHealth repos, verified green across their
// full matrices.
//
// Caveat, recorded 2026-08-13 so nobody re-derives it: excluding junit-fml is the
// right call *for these repos*, not a universal one. junit-fml is precisely
// NeoForge's own loaded-test bootstrap - it is what stands FML up so a test can
// run against a real, loaded game. NeoForge's supported loaded-test path
// (`neoForge { unitTest { enable(); testedMod = ... } }`, the `testframework`
// artifact, `@ExtendWith(EphemeralTestServerProvider.class)`, `runGameTestServer`)
// is ModDevGradle-only, and these repos build on Architectury Loom via Stonecraft,
// so that path is unavailable here regardless. Excluding junit-fml therefore costs
// nothing today. If a cell is ever migrated to ModDevGradle, this exclusion must
// be revisited before writing any loaded NeoForge test - it would silently disable
// the very bootstrap such a test depends on.
if (mod.isNeoforge) {
    configurations.named("testRuntimeClasspath") {
        exclude(group = "net.neoforged.fancymodloader", module = "junit-fml")
    }
}

// Forge's pack.mcmeta generation task writes into the main source set's resources output
// without declaring that as a tracked task output, which Gradle's task validation flags as
// an undeclared ("implicit") dependency on :compileTestJava (which consumes sourceSets.main.output
// as part of the test compile classpath). Declare the dependency explicitly so validation passes.
// tasks.matching(...) is a live/lazy filter, so this is a harmless no-op on loaders that don't
// have a generatePackMCMetaJson task (e.g. Fabric).
tasks.matching { it.name == "compileTestJava" }.configureEach {
    dependsOn(tasks.matching { it.name == "generatePackMCMetaJson" })
}

// Publishing configuration
publishMods {
    modrinth {
        if (mod.isFabric) requires("fabric-api")
    }

    curseforge {
        client = true
        server = false
        if (mod.isFabric) requires("fabric-api")
    }
}
