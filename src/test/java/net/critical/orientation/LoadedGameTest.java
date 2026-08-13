package net.critical.orientation;

//? if fabric {
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tier 1 "loaded game" tests: these run against a real, bootstrapped
 * Minecraft rather than mocks, courtesy of fabric-loader-junit (see the
 * dependency comment in build.gradle.kts).
 *
 * <p>The point is not to re-test {@link OrientationCommon}'s arithmetic -
 * OrientationCommonTest already does that headless and far faster. The point
 * is to pin that arithmetic to the game's <em>own</em> notion of direction, so
 * that a future Minecraft version quietly changing how yaw maps to a compass
 * facing fails the build instead of silently making the keybind snap the
 * player the wrong way.
 *
 * <p>Fabric cells only: NeoForge's equivalent bootstrap (junit-fml) is only
 * usable from ModDevGradle, not from Architectury Loom - see the junit-fml
 * exclusion comment in build.gradle.kts.
 */
public class LoadedGameTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void gameDataIsActuallyLoaded() {
        // Guard on the harness itself: if the bootstrap above ever silently
        // no-ops, every other assertion in this class becomes vacuous.
        assertNotNull(Items.DIAMOND, "Items.DIAMOND should be a real loaded game object");
        // The item registry moved from Registry.ITEM to BuiltInRegistries.ITEM
        // in 1.19.3; both expose getKey/keySet identically.
        //? if >=1.19.3 {
        var itemRegistry = net.minecraft.core.registries.BuiltInRegistries.ITEM;
        //?} else
        /*var itemRegistry = net.minecraft.core.Registry.ITEM;*/
        assertEquals("minecraft:diamond", itemRegistry.getKey(Items.DIAMOND).toString());
        assertTrue(itemRegistry.keySet().size() > 500,
                "the real item registry should hold the full vanilla item set");
    }

    @Test
    void snappedYawAgreesWithTheGamesOwnCardinalDirections() {
        // Direction.fromYRot is vanilla's own yaw -> facing mapping, the same
        // one the player's rendered facing follows. Every yaw the mod can
        // produce for a cardinal point must land on that exact facing.
        assertEquals(Direction.SOUTH, Direction.fromYRot(OrientationCommon.snapYaw(0)));
        assertEquals(Direction.WEST, Direction.fromYRot(OrientationCommon.snapYaw(90)));
        assertEquals(Direction.NORTH, Direction.fromYRot(OrientationCommon.snapYaw(180)));
        assertEquals(Direction.EAST, Direction.fromYRot(OrientationCommon.snapYaw(-90)));
    }

    @Test
    void nearMissYawSnapsToTheCardinalTheGameWouldRoundTo() {
        // The mod's whole purpose: an almost-north look becomes exactly north.
        // Asserted through Direction so it is the game, not the test, that
        // decides what "north" means.
        assertEquals(Direction.NORTH, Direction.fromYRot(OrientationCommon.snapYaw(178.4)));
        assertEquals(Direction.NORTH, Direction.fromYRot(OrientationCommon.snapYaw(-178.4)));
        assertEquals(Direction.SOUTH, Direction.fromYRot(OrientationCommon.snapYaw(3.2)));
    }
}
//?}
