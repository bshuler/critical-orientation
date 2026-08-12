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
