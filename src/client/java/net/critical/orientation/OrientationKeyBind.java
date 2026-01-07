package net.critical.orientation;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class OrientationKeyBind {
    private static final String CATEGORY = "category.orientation.main";

    private static KeyBinding keyBinding;

    public static void register() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.orientation.snap",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_BACKSLASH,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(OrientationKeyBind::onClientTick);
    }

    private static void onClientTick(MinecraftClient client) {
        while (keyBinding.wasPressed()) {
            if (client.player != null) {
                double yaw = client.player.getHeadYaw();
                yaw = roundYaw(normalizeHeadYaw(yaw));

                client.player.setYaw((float) yaw);
                client.player.setHeadYaw((float) yaw);
                client.player.setBodyYaw((float) yaw);
            }
        }
    }

    /**
     * Normalizes a yaw angle to the range -180 to 180 degrees.
     *
     * @param yaw the input yaw angle
     * @return the normalized yaw angle
     */
    public static double normalizeHeadYaw(double yaw) {
        yaw = yaw % 360;
        if (yaw > 180) {
            yaw -= 360;
        } else if (yaw < -180) {
            yaw += 360;
        }
        return yaw;
    }

    /**
     * Rounds a yaw angle to the nearest 45-degree increment (cardinal or intercardinal direction).
     * <p>
     * Directions: N (0°), NE (45°), E (90°), SE (135°), S (180°/-180°), SW (-135°), W (-90°), NW (-45°)
     *
     * @param yaw the input yaw angle (should be normalized to -180 to 180)
     * @return the rounded yaw angle
     */
    public static double roundYaw(double yaw) {
        // Positive yaw (0 to 180)
        if (yaw >= 0 && yaw < 22.5) return 0;
        if (yaw >= 22.5 && yaw < 67.5) return 45;
        if (yaw >= 67.5 && yaw < 112.5) return 90;
        if (yaw >= 112.5 && yaw < 157.5) return 135;
        if (yaw >= 157.5 && yaw <= 180) return 180;

        // Negative yaw (-180 to 0)
        if (yaw <= 0 && yaw > -22.5) return 0;
        if (yaw <= -22.5 && yaw > -67.5) return -45;
        if (yaw <= -67.5 && yaw > -112.5) return -90;
        if (yaw <= -112.5 && yaw > -157.5) return -135;
        if (yaw <= -157.5 && yaw >= -180) return 180;

        return yaw;
    }
}
