package net.critical.orientation;

//? if fabric {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//?} elif neoforge {
/*import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ClientTickEvent;
*///?} elif forge {
/*import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
*///?}

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class OrientationKeyBind {
    private static final String CATEGORY = "category.orientation.main";

    private static KeyBinding keyBinding;

    public static void register() {
        keyBinding = new KeyBinding(
                "key.orientation.snap",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_BACKSLASH,
                CATEGORY
        );

        //? if fabric {
        KeyBindingHelper.registerKeyBinding(keyBinding);
        ClientTickEvents.END_CLIENT_TICK.register(OrientationKeyBind::onClientTick);
        //?} elif neoforge {
        /*// NeoForge keybind registration is handled via RegisterKeyMappingsEvent
        NeoForge.EVENT_BUS.addListener(OrientationKeyBind::onClientTickNeoForge);
        *///?} elif forge {
        /*// Forge keybind registration is handled via RegisterKeyMappingsEvent
        MinecraftForge.EVENT_BUS.addListener(OrientationKeyBind::onClientTickForge);
        *///?}
    }

    //? if neoforge {
    /*public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(keyBinding);
    }

    private static void onClientTickNeoForge(ClientTickEvent.Post event) {
        onClientTick(MinecraftClient.getInstance());
    }
    *///?} elif forge {
    /*public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(keyBinding);
    }

    private static void onClientTickForge(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            onClientTick(MinecraftClient.getInstance());
        }
    }
    *///?}

    private static void onClientTick(MinecraftClient client) {
        while (keyBinding.wasPressed()) {
            if (client.player != null) {
                float snappedYaw = OrientationCommon.snapYaw(client.player.getHeadYaw());

                client.player.setYaw(snappedYaw);
                client.player.setHeadYaw(snappedYaw);
                client.player.setBodyYaw(snappedYaw);
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
