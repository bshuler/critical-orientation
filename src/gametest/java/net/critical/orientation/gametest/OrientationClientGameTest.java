package net.critical.orientation.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;

/**
 * Tier 3: this mod running inside a <strong>real Minecraft client</strong> --
 * real window, real GL context, real world, a real player, and a real backslash
 * keystroke arriving through the real keyboard handler.
 *
 * <p>This mod is one gesture: press a key, the view snaps to the nearest 45
 * degrees. The arithmetic behind that gesture ({@code OrientationCommon}) is
 * pure, loader-free and already held to 100% line coverage by the headless
 * suite. <strong>Everything else about the gesture had no coverage at all
 * before this file.</strong> {@code OrientationKeyBind} -- the
 * {@code KeyMapping} itself, the per-loader registration call, the client-tick
 * listener, the {@code consumeClick()} drain loop, and the three
 * {@code setYRot}/{@code setYHeadRot}/{@code setYBodyRot} writes -- is on the
 * JaCoCo exclusion list for a sound reason: merely loading the class runs a
 * static initializer that calls {@code KeyMapping.Category.register(...)}
 * against live Minecraft registry classes. Tier 1 (fabric-loader-junit)
 * bootstraps registries but has no window, no input pipeline, no tick loop and
 * no player, so it cannot reach any of it either.
 *
 * <p>The size of that hole is worth stating plainly. Delete the
 * {@code ClientTickEvents.END_CLIENT_TICK.register(...)} line, or the
 * {@code KeyMappingHelper.registerKeyMapping(...)} line, and this repo stays
 * completely green: it compiles on all thirty cells, packages thirty jars,
 * passes every headless test and every loaded-game test -- and the mod does
 * nothing whatsoever when you press the key. Tier 3 is the only tier that
 * notices.
 *
 * <h2>What is actually asserted</h2>
 *
 * <ol>
 *   <li><strong>The keybind reached vanilla.</strong> The mod's
 *       {@code KeyMapping} is present in the live
 *       {@code Minecraft.options.keyMappings} array and is bound to backslash.
 *       Fabric's {@code KeyMappingHelper}/{@code KeyBindingHelper} works by
 *       appending to exactly that array, so its absence there is registration
 *       failure, full stop.</li>
 *   <li><strong>Nothing snaps on its own.</strong> An unaligned yaw is set and
 *       left alone for a second of real ticks; it must still be unaligned.
 *       Without this, a mod that snapped every tick regardless of input would
 *       pass every case below.</li>
 *   <li><strong>The real keystroke snaps the view.</strong> Thirteen starting
 *       yaws, one raw backslash press each, asserted on the player's actual
 *       {@code yRot}/{@code yHeadRot}/{@code yBodyRot} afterwards.</li>
 *   <li><strong>The drain loop is idempotent.</strong> Two presses before a
 *       single tick, so {@code while (consumeClick())} runs twice in one pass.</li>
 * </ol>
 *
 * <h2>The key is pressed raw, not through the mod's own KeyMapping</h2>
 *
 * <p>{@code TestInput} offers {@code pressKey(KeyMapping)}, which would be
 * shorter. It is deliberately not used. Handing the harness the mod's own
 * mapping object proves only that the object exists; pressing
 * {@code GLFW_KEY_BACKSLASH} as a bare keysym is what a user does, and it
 * reaches the mapping only by travelling the whole real path --
 * {@code KeyboardHandler.keyPress} -> {@code KeyMapping.click(key)} -> the
 * static key-to-mapping map that vanilla rebuilds from
 * {@code Options.keyMappings}. A mapping that was constructed but never
 * registered is simply not in that map, and no click ever lands on it. The
 * separate binding assertion (1) exists so that a *rebound* key fails with
 * "bound to X, expected backslash" rather than with a silent non-snap.
 *
 * <h2>Why there are no game rules here</h2>
 *
 * <p>The sibling repos EasierVillagerTrading and ToroHealth open their staging
 * with six {@code gamerule} commands, and on 26.2 all six silently failed for a
 * full run -- {@code TestServerContext#runCommand} swallows command failures,
 * and 26.2 had renamed every rule to snake_case and changed three of their
 * identities outright. Nothing caught it because nothing depended on it.
 *
 * <p>The lesson taken here is not "port the rename"; it is "do not stage what
 * the test does not need". A yaw does not care about daylight, weather, mob
 * spawning or fire. So the staging is a single command,
 * {@code gamemode creative @p} -- and even that one is <em>verified to have
 * taken effect</em> before anything is measured, by waiting on
 * {@code player.isCreative()}. An unverified setup command is not a
 * convenience; it is a control that silently stops being enforced.
 *
 * <h2>One file, twelve cells, no Stonecutter branch</h2>
 *
 * <p>Every cell from 1.21.4 to 26.2 can run this, spanning
 * fabric-client-gametest-api v4.1.1 through v6.0.0. {@code ClientGameTestContext},
 * {@code FabricClientGameTest}, {@code TestWorldBuilder}, {@code TestServerContext}
 * and {@code TestInput} were checked with javap against every API jar in the
 * matrix and are method-identical across all of them. The one interface that did
 * change is {@code TestSingleplayerContext} ({@code getClientWorld()} became
 * {@code getClientLevel()} at v5 and {@code getConnection()} at v6); none of the
 * three is used here. On the Minecraft side, this file names only
 * {@code KeyMapping}, {@code InputConstants}, {@code TitleScreen} and the
 * rotation accessors -- all of which the mod's own production code already calls
 * unconditionally on all thirty cells, so their stability is established by the
 * build itself rather than assumed here.
 */
public class OrientationClientGameTest implements FabricClientGameTest {

    private static final String MOD_ID = "orientation";

    /** The translation key {@code OrientationKeyBind} constructs its mapping with. */
    private static final String KEYBIND_NAME = "key.orientation.snap";

    /**
     * {@code KeyMapping#saveString()} for {@code GLFW_KEY_BACKSLASH}. Read off
     * the real jar's {@code InputConstants.Type} initializer rather than guessed;
     * it is a vanilla lang key and has not moved across this matrix.
     */
    private static final String EXPECTED_BINDING = "key.keyboard.backslash";

    /** Ticks to let the world, the player and the gamemode change settle. */
    private static final int SETTLE_TICKS = 20;

    /**
     * Ticks to wait for the snap. One would do -- the listener is on
     * {@code END_CLIENT_TICK} -- but the extra ticks also give anything that
     * might undo the snap a chance to do so before it is measured.
     */
    private static final int SNAP_TICKS = 5;

    /** Tolerance on a float yaw comparison. The expected values are exact. */
    private static final float EPSILON = 1.0e-4f;

    /**
     * How long the no-keypress control waits. Minecraft's own
     * {@code SharedConstants.TICKS_PER_SECOND} would say the same thing, but it
     * is server-side data this client test has no other reason to touch.
     */
    private static final int ONE_SECOND_TICKS = 20;

    /**
     * {starting yaw, expected snapped yaw}. Chosen to cover every bucket
     * boundary in {@code OrientationCommon.roundYaw} and both wrap branches of
     * {@code normalizeHeadYaw}, because the pure suite proves the arithmetic in
     * isolation while this file proves the *live player's rotation* ends up
     * there:
     *
     * <ul>
     *   <li>22.5 and 67.5 are inclusive lower edges -- an off-by-one comparison
     *       flips them into the bucket below.</li>
     *   <li>-157.5 snaps to +180, not -180. Those are the same heading and
     *       different floats; the mod writes the positive one.</li>
     *   <li>350, 730 and -395 are outside [-180, 180] entirely. The client will
     *       happily hold a yaw of 730 -- vanilla does not normalize on
     *       {@code setYRot} -- so these exercise {@code normalizeHeadYaw}
     *       against a genuinely denormalized live player rather than a
     *       hand-fed double.</li>
     * </ul>
     */
    private static final float[][] SNAP_CASES = {
        {0.0f, 0.0f},
        {12.3f, 0.0f},
        {22.5f, 45.0f},
        {44.0f, 45.0f},
        {67.4f, 45.0f},
        {67.5f, 90.0f},
        {100.0f, 90.0f},
        {134.9f, 135.0f},
        {179.0f, 180.0f},
        {-100.0f, -90.0f},
        {-157.5f, 180.0f},
        {350.0f, 0.0f},
        {730.0f, 0.0f},
        {-395.0f, -45.0f},
    };

    @Override
    public void runTest(ClientGameTestContext context) {
        context.waitForScreen(TitleScreen.class);
        context.takeScreenshot("orientation-title-screen");

        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) {
            throw new AssertionError("the client booted without the '" + MOD_ID
                    + "' mod loaded - everything below this point would have passed vacuously");
        }

        assertKeybindIsRegisteredAndBoundToBackslash(context);

        try (TestSingleplayerContext singleplayer =
                     context.worldBuilder().setUseConsistentSettings(true).create()) {
            singleplayer.getServer().runCommand("gamemode creative @p");
            awaitCreativeMode(context);

            buildLandmark(context, singleplayer);

            context.waitTicks(SETTLE_TICKS);
            context.takeScreenshot("orientation-world-ready");

            assertNothingSnapsWithoutTheKey(context);

            for (float[] testCase : SNAP_CASES) {
                assertPressSnapsYaw(context, testCase[0], testCase[1]);
            }

            // A picture of one snap for the human reading the CI artifacts:
            // 67.5 degrees, then the key, then 90. The gold pillar is due west,
            // which is exactly yaw 90, so the "after" shot has it dead under the
            // crosshair and the "before" shot has it well off to one side.
            setYaw(context, 67.5f);
            context.waitTicks(SNAP_TICKS);
            context.takeScreenshot("orientation-before-snap");
            pressSnapKey(context);
            context.waitTicks(SNAP_TICKS);
            context.takeScreenshot("orientation-after-snap");

            assertRepeatedPressIsIdempotent(context);

            // Keep the singleplayer context referenced until here so it is
            // unambiguous that every assertion above ran with the world open.
            singleplayer.getWorldSave();
        }

        // The client gametest runner requires the test to end on the title
        // screen with no server running and no connection open.
        context.waitForScreen(TitleScreen.class);
    }

    // ------------------------------------------------------------------
    // Assertions
    // ------------------------------------------------------------------

    /**
     * Confirms the mod's {@code KeyMapping} actually reached vanilla's registry
     * and carries the binding this mod advertises.
     *
     * <p>This runs before the world is created, and before any key is pressed,
     * so that the two failure modes stay distinguishable. "Not registered" and
     * "registered but the tick listener never fires" both look identical from
     * the far end -- a yaw that did not move -- and they have entirely different
     * causes (a per-loader registration branch versus the event subscription).
     */
    private static void assertKeybindIsRegisteredAndBoundToBackslash(ClientGameTestContext context) {
        String binding = context.computeOnClient(client -> {
            for (KeyMapping mapping : client.options.keyMappings) {
                if (KEYBIND_NAME.equals(mapping.getName())) {
                    return mapping.saveString();
                }
            }
            return null;
        });

        if (binding == null) {
            List<String> registered = new ArrayList<>();
            context.runOnClient(client -> {
                for (KeyMapping mapping : client.options.keyMappings) {
                    registered.add(mapping.getName());
                }
            });
            throw new AssertionError("'" + KEYBIND_NAME + "' is not in the live client's "
                    + "Options.keyMappings array, so the mod's key binding was never registered - "
                    + "vanilla's key-to-mapping lookup is rebuilt from exactly this array, so no "
                    + "keystroke can ever reach the mod. Check OrientationKeyBind.register()'s "
                    + "per-loader registration branch (KeyMappingHelper on >=26.1, "
                    + "KeyBindingHelper below it). The client did register "
                    + registered.size() + " mappings: " + registered);
        }

        if (!EXPECTED_BINDING.equals(binding)) {
            throw new AssertionError("'" + KEYBIND_NAME + "' is registered but bound to '"
                    + binding + "', not '" + EXPECTED_BINDING + "'. Every assertion below presses "
                    + "a raw backslash keysym, which is what a user with default options presses, "
                    + "so they would all fail for a reason that has nothing to do with the snap "
                    + "logic. Either the default in OrientationKeyBind changed, or something wrote "
                    + "an options.txt into the gametest run directory.");
        }
    }

    /**
     * Waits until the client sees itself in creative mode.
     *
     * <p>Not politeness -- enforcement. {@code runCommand} does not throw when a
     * command fails (verified in the bytecode of
     * {@code TestServerContextImpl#runCommand}: it calls
     * {@code performPrefixedCommand}, which logs and returns). A staging command
     * that quietly stopped working would leave the player in survival, which is
     * survivable for this particular test right up until the run where it isn't.
     * So the one command this test issues is confirmed by its observable effect.
     */
    private static void awaitCreativeMode(ClientGameTestContext context) {
        try {
            context.waitFor(client -> client.player != null && client.player.isCreative());
        } catch (RuntimeException e) {
            throw new AssertionError("the player never entered creative mode after "
                    + "'gamemode creative @p'. runCommand swallows command failures, so the "
                    + "command most likely did not parse or did not resolve @p - read the server "
                    + "log in the run directory, not the exit code.", e);
        }
    }

    /**
     * Puts one gold pillar ten blocks due west of the player, and confirms the
     * client can see it.
     *
     * <p>This exists for the two screenshots, and it exists because the first run
     * of this test produced screenshots that were worthless. A
     * consistent-settings world is a featureless grass plain under a clear sky:
     * a "before snap" and an "after snap" shot of it are pixel-for-pixel
     * indistinguishable no matter how far the view turned, so a human reviewing
     * the CI artifacts learns nothing and -- worse -- learns nothing while
     * feeling informed. A landmark makes the rotation legible.
     *
     * <p>Due west is chosen because west <em>is</em> yaw 90 (Minecraft's yaw 0 is
     * south / +Z, and it increases clockwise toward -X), so the pillar sits
     * exactly on one of the eight snap targets. The "after" screenshot therefore
     * has it centred under the crosshair, and any snap that lands off-target is
     * visibly off-centre rather than merely a number in a log.
     *
     * <p>The command is wrapped in {@code execute as @p at @s} because
     * {@code runCommand} dispatches from the world spawn, not from the player --
     * a bare {@code fill ~-10 …} would build the pillar somewhere else entirely.
     * And because {@code runCommand} swallows failures, the pillar is not assumed
     * to exist: the test waits until the <em>client</em> reports gold at that
     * position, which also disposes of the chunk-sync race that a bare
     * "the server ran it" check would leave open.
     */
    private static void buildLandmark(ClientGameTestContext context, TestSingleplayerContext singleplayer) {
        singleplayer.getServer().runCommand(
                "execute as @p at @s run fill ~-10 ~-1 ~ ~-10 ~4 ~ gold_block");

        try {
            context.waitFor(client -> client.player != null
                    && client.player.level()
                    .getBlockState(client.player.blockPosition().offset(-10, 0, 0))
                    .getBlock() == Blocks.GOLD_BLOCK);
        } catch (RuntimeException e) {
            throw new AssertionError("the gold landmark never appeared on the client ten blocks "
                    + "west of the player. runCommand swallows command failures, so the fill "
                    + "likely did not parse on this version - read the server log in the run "
                    + "directory. Nothing below this point is wrong because of it, but the two "
                    + "snap screenshots would be a featureless grass plain and would show a human "
                    + "reviewer nothing at all.", e);
        }
    }

    /**
     * The in-test control: with no key pressed, an unaligned yaw must stay
     * unaligned.
     *
     * <p>Every other assertion here checks that a yaw <em>became</em> a multiple
     * of 45 degrees. A mod that snapped unconditionally on every client tick,
     * ignoring the keybind entirely, would satisfy all of them. This is the
     * assertion that says the keystroke is the cause.
     */
    private static void assertNothingSnapsWithoutTheKey(ClientGameTestContext context) {
        float unaligned = 12.3f;
        setYaw(context, unaligned);
        context.waitTicks(ONE_SECOND_TICKS);

        float actual = context.computeOnClient(client -> client.player.getYRot());
        if (Math.abs(actual - unaligned) > EPSILON) {
            throw new AssertionError(String.format(Locale.ROOT,
                    "the player's yaw moved from %.4f to %.4f over %d ticks with no key pressed. "
                            + "Either something is snapping unconditionally - in which case every "
                            + "snap assertion in this file is vacuous, because they cannot tell a "
                            + "keystroke from a tick - or the test's own staging is disturbing the "
                            + "player.",
                    unaligned, actual, ONE_SECOND_TICKS));
        }
    }

    /**
     * The flagship: set a yaw, press the real key, assert the live player's
     * rotation snapped.
     *
     * <p>All three rotation fields are checked, not just {@code yRot}. The mod
     * writes {@code setYRot}, {@code setYHeadRot} and {@code setYBodyRot}
     * because writing only the first leaves the head and body lagging visibly
     * behind the camera for a tick or two; asserting only the first would not
     * notice if the other two were dropped.
     */
    private static void assertPressSnapsYaw(ClientGameTestContext context, float from, float expected) {
        setYaw(context, from);
        context.waitTicks(SNAP_TICKS);

        float before = context.computeOnClient(client -> client.player.getYRot());
        if (Math.abs(before - from) > EPSILON) {
            throw new AssertionError(String.format(Locale.ROOT,
                    "staging failed before the key was even pressed: asked for yaw %.4f, the live "
                            + "player reports %.4f. The snap assertion below would be measuring "
                            + "something other than what it claims.",
                    from, before));
        }

        pressSnapKey(context);
        context.waitTicks(SNAP_TICKS);

        float yRot = context.computeOnClient(client -> client.player.getYRot());
        float yHeadRot = context.computeOnClient(client -> client.player.getYHeadRot());
        float yBodyRot = context.computeOnClient(client -> client.player.yBodyRot);

        if (Math.abs(yRot - expected) > EPSILON) {
            throw new AssertionError(String.format(Locale.ROOT,
                    "pressing backslash at yaw %.4f left the player at yaw %.4f; expected %.4f. "
                            + "%s",
                    from, yRot, expected,
                    Math.abs(yRot - from) <= EPSILON
                            ? "The yaw did not move at all, so the keystroke never reached "
                              + "OrientationKeyBind.onClientTick - the mapping is registered (that "
                              + "was asserted before the world opened), so suspect the client-tick "
                              + "listener registration or the consumeClick() drain."
                            : "The yaw did move, so the keystroke was received and the snap ran - "
                              + "this is OrientationCommon's arithmetic disagreeing with the pure "
                              + "suite, or a normalization branch that only triggers on a live "
                              + "denormalized player."));
        }

        if (Math.abs(yHeadRot - expected) > EPSILON) {
            throw new AssertionError(String.format(Locale.ROOT,
                    "yaw snapped to %.4f but the head is still at %.4f (expected %.4f) - "
                            + "setYHeadRot was not applied, so the camera and the visible head "
                            + "disagree.",
                    yRot, yHeadRot, expected));
        }

        if (Math.abs(yBodyRot - expected) > EPSILON) {
            throw new AssertionError(String.format(Locale.ROOT,
                    "yaw snapped to %.4f but the body is still at %.4f (expected %.4f) - "
                            + "setYBodyRot was not applied.",
                    yRot, yBodyRot, expected));
        }
    }

    /**
     * Two presses landing in the same tick.
     *
     * <p>{@code onClientTick} drains with {@code while (keyBinding.consumeClick())},
     * so a burst is processed in one pass. Snapping an already-snapped yaw is the
     * identity, which makes the expected result unambiguous: the second iteration
     * must neither double-rotate nor undo the first.
     */
    private static void assertRepeatedPressIsIdempotent(ClientGameTestContext context) {
        setYaw(context, 100.0f);
        context.waitTicks(SNAP_TICKS);

        pressSnapKey(context);
        pressSnapKey(context);
        context.waitTicks(SNAP_TICKS);

        float actual = context.computeOnClient(client -> client.player.getYRot());
        if (Math.abs(actual - 90.0f) > EPSILON) {
            throw new AssertionError(String.format(Locale.ROOT,
                    "two backslash presses from yaw 100 left the player at %.4f; expected 90 "
                            + "(snapping an already-snapped yaw is the identity). The "
                            + "while (consumeClick()) drain in onClientTick is mishandling a "
                            + "burst.",
                    actual));
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Puts the live player at a given yaw.
     *
     * <p>Set client-side rather than with {@code /tp}, on purpose. Rotation is
     * client-authoritative in Minecraft, so this is the same state a mouse
     * movement would produce, and it sidesteps a whole category of staging bug:
     * {@code runCommand} would silently do nothing if the command's syntax
     * shifted between versions, and this test would then measure a yaw nobody
     * set. Head and body are set alongside {@code yRot} so the starting state is
     * self-consistent -- vanilla re-syncs the head to the body each tick, and a
     * mismatch would make the "before" reading ambiguous.
     */
    private static void setYaw(ClientGameTestContext context, float yaw) {
        context.runOnClient(client -> {
            client.player.setYRot(yaw);
            client.player.setYHeadRot(yaw);
            client.player.yBodyRot = yaw;
        });
    }

    /**
     * Presses backslash as a raw keysym, through the client's real keyboard
     * handler. See the class doc for why this is not
     * {@code pressKey(theModsKeyMapping)}.
     */
    private static void pressSnapKey(ClientGameTestContext context) {
        context.getInput().pressKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_BACKSLASH));
    }
}
