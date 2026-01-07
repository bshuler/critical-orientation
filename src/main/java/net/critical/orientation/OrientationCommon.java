package net.critical.orientation;

/**
 * Common initialization and shared logic for Critical Orientation mod.
 * This class is loader-agnostic and called from loader-specific entry points.
 */
public class OrientationCommon {

    public static final String MOD_ID = "orientation";

    /**
     * Common initialization called from all loader entry points.
     */
    public static void init() {
        // Common initialization logic (if any)
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

    /**
     * Snaps a yaw angle to the nearest cardinal or intercardinal direction.
     *
     * @param yaw the input yaw angle
     * @return the snapped yaw angle
     */
    public static float snapYaw(double yaw) {
        return (float) roundYaw(normalizeHeadYaw(yaw));
    }
}
