package net.critical.orientation;

//? if fabric {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//?}
//? if fabric && >=26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?} elif fabric {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
*///?}
// ClientTickEvent has always lived under net.neoforged.neoforge.client.event (confirmed via jar
// inspection on NeoForge 21.4.157 through 26.2.0.58 - it never lived under
// net.neoforged.neoforge.event.tick, which only holds Entity/Level/Player/Server tick events).
// Forge < 1.19 (only 1.18.2 in this matrix) has no RegisterKeyMappingsEvent; key bindings are
// registered directly via ClientRegistry.registerKeyBinding(KeyMapping) instead (confirmed via
// jar inspection: RegisterKeyMappingsEvent was introduced for Forge 1.19+; net.minecraftforge.client.ClientRegistry
// is the pre-1.19 mechanism).
// NOTE: explanatory comments like these must live OUTSIDE any //? if/elif block (i.e. before the
// marker line), never as the first lines inside a block's content region - Stonecutter's
// comment-toggle generator strips a leading "//" from every plain-comment line it finds inside a
// disabled span when re-enabling that span for another target, which corrupts the comment into
// bare invalid text (confirmed by inspecting the generated 1.18.2-forge tree after this bug).
//? if neoforge {
/*import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
*///?} elif forge && >=1.19 {
/*import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
*///?} elif forge {
/*import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
*///?}

// Mojang official mappings are used across the whole matrix (Stonecraft's default
// namespace); Yarn/Intermediary are Fabric-tooling-only and are not published at all
// for Minecraft 26.1+, so this codebase never depends on them.
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
// KeyMapping's 4th constructor argument changed from a plain String to a proper
// KeyMapping.Category record type starting at MC 1.21.9 (confirmed via javap across the
// full version matrix: 1.21.4-1.21.8 take String; 1.21.9 onward take KeyMapping.Category).
// Building a custom Category requires KeyMapping.Category.register(<namespaced id>), and the
// id type itself was renamed ResourceLocation -> Identifier starting at MC 1.21.11 (confirmed
// via jar-content inspection: 1.21.9/1.21.10 ship ResourceLocation, 1.21.11+ ship Identifier).
// This is independent of the Fabric KeyBindingHelper -> KeyMappingHelper package rename above,
// which lands at a different boundary (>=26.1) - the old Fabric helper's registerKeyBinding(KeyMapping)
// is agnostic to the internal category field's type, so no coupling between the two exists.
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} elif >=1.21.9 {
/*import net.minecraft.resources.ResourceLocation;
*///?}

public class OrientationKeyBind {
    //? if >=1.21.11 {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("orientation", "main"));
    //?} elif >=1.21.9 {
    /*private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("orientation", "main"));
    *///?} else {
    /*private static final String CATEGORY = "category.orientation.main";
    *///?}

    private static KeyMapping keyBinding;

    public static void register() {
        keyBinding = new KeyMapping(
                "key.orientation.snap",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_BACKSLASH,
                CATEGORY
        );

        //? if fabric && >=26.1 {
        KeyMappingHelper.registerKeyMapping(keyBinding);
        //?} elif fabric {
        /*KeyBindingHelper.registerKeyBinding(keyBinding);
        *///?} elif forge && <1.19 {
        /*// Forge < 1.19 has no RegisterKeyMappingsEvent; register directly.
        ClientRegistry.registerKeyBinding(keyBinding);
        *///?}
        // NeoForge and Forge >=1.19 register via RegisterKeyMappingsEvent instead (see
        // registerKeyMappings below), since that event fires after this method's caller
        // (FMLClientSetupEvent) on those loaders.
        //? if fabric {
        ClientTickEvents.END_CLIENT_TICK.register(OrientationKeyBind::onClientTick);
        //?} elif neoforge {
        /*// NeoForge keybind registration is handled via RegisterKeyMappingsEvent
        NeoForge.EVENT_BUS.addListener(OrientationKeyBind::onClientTickNeoForge);
        *///?} elif forge {
        /*// Forge keybind registration is handled via RegisterKeyMappingsEvent (>=1.19) or above
        // (<1.19); either way the tick listener is registered the same way on both.
        MinecraftForge.EVENT_BUS.addListener(OrientationKeyBind::onClientTickForge);
        *///?}
    }

    //? if neoforge {
    /*public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(keyBinding);
    }

    private static void onClientTickNeoForge(ClientTickEvent.Post event) {
        onClientTick(Minecraft.getInstance());
    }
    *///?} elif forge && >=1.19 {
    /*public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(keyBinding);
    }

    private static void onClientTickForge(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            onClientTick(Minecraft.getInstance());
        }
    }
    *///?} elif forge {
    /*private static void onClientTickForge(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            onClientTick(Minecraft.getInstance());
        }
    }
    *///?}

    private static void onClientTick(Minecraft client) {
        while (keyBinding.consumeClick()) {
            if (client.player != null) {
                float snappedYaw = OrientationCommon.snapYaw(client.player.getYHeadRot());

                client.player.setYRot(snappedYaw);
                client.player.setYHeadRot(snappedYaw);
                client.player.setYBodyRot(snappedYaw);
            }
        }
    }

    // Delegate to common for testing
    public static double normalizeHeadYaw(double yaw) {
        return OrientationCommon.normalizeHeadYaw(yaw);
    }

    public static double roundYaw(double yaw) {
        return OrientationCommon.roundYaw(yaw);
    }
}
