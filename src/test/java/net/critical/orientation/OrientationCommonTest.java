package net.critical.orientation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrientationCommonTest {

    @Nested
    @DisplayName("normalizeHeadYaw")
    class NormalizeHeadYawTests {

        @Test
        @DisplayName("should return 0 for 0 input")
        void testZero() {
            assertEquals(0, OrientationCommon.normalizeHeadYaw(0), 0.001);
        }

        @Test
        @DisplayName("should return 0 for 360 input")
        void testFullRotation() {
            assertEquals(0, OrientationCommon.normalizeHeadYaw(360), 0.001);
        }

        @Test
        @DisplayName("should return 0 for -360 input")
        void testNegativeFullRotation() {
            assertEquals(0, OrientationCommon.normalizeHeadYaw(-360), 0.001);
        }

        @ParameterizedTest
        @DisplayName("should normalize positive overflow angles")
        @CsvSource({
            "450, 90",
            "540, 180",
            "720, 0",
            "810, 90"
        })
        void testPositiveOverflow(double input, double expected) {
            assertEquals(expected, OrientationCommon.normalizeHeadYaw(input), 0.001);
        }

        @ParameterizedTest
        @DisplayName("should normalize negative overflow angles")
        @CsvSource({
            "-450, -90",
            "-540, -180",
            "-720, 0",
            "-810, -90"
        })
        void testNegativeOverflow(double input, double expected) {
            assertEquals(expected, OrientationCommon.normalizeHeadYaw(input), 0.001);
        }

        // Java's % operator preserves the dividend's sign and can return a result whose magnitude
        // is up to (but not including) 360 - e.g. 270 % 360 == 270, which is already > 180 without
        // ever needing an extra full-rotation subtraction first. These cases exercise the ">180" /
        // "<-180" post-modulo adjustment branches directly; the multiples-of-90/180 cases above never
        // trigger them because they all divide down to values already inside [-180, 180].
        @ParameterizedTest
        @DisplayName("should subtract 360 when the post-modulo result exceeds 180")
        @CsvSource({
            "270, -90",
            "200, -160",
            "181, -179"
        })
        void testPostModuloAbove180(double input, double expected) {
            assertEquals(expected, OrientationCommon.normalizeHeadYaw(input), 0.001);
        }

        @ParameterizedTest
        @DisplayName("should add 360 when the post-modulo result is below -180")
        @CsvSource({
            "-270, 90",
            "-200, 160",
            "-181, 179"
        })
        void testPostModuloBelowNegative180(double input, double expected) {
            assertEquals(expected, OrientationCommon.normalizeHeadYaw(input), 0.001);
        }

        @ParameterizedTest
        @DisplayName("should pass through values already in range")
        @CsvSource({
            "90, 90",
            "-90, -90",
            "180, 180",
            "-180, -180",
            "45, 45",
            "-45, -45"
        })
        void testInRangeValues(double input, double expected) {
            assertEquals(expected, OrientationCommon.normalizeHeadYaw(input), 0.001);
        }
    }

    @Nested
    @DisplayName("roundYaw")
    class RoundYawTests {

        @Nested
        @DisplayName("Cardinal directions")
        class CardinalTests {

            @ParameterizedTest
            @DisplayName("should round to North (0°)")
            @CsvSource({
                "0, 0",
                "10, 0",
                "22.4, 0",
                "-10, 0",
                "-22.4, 0"
            })
            void testNorth(double input, double expected) {
                assertEquals(expected, OrientationCommon.roundYaw(input), 0.001);
            }

            @ParameterizedTest
            @DisplayName("should round to East (90°)")
            @CsvSource({
                "90, 90",
                "67.5, 90",
                "112.4, 90",
                "80, 90"
            })
            void testEast(double input, double expected) {
                assertEquals(expected, OrientationCommon.roundYaw(input), 0.001);
            }

            @ParameterizedTest
            @DisplayName("should round to South (180°)")
            @CsvSource({
                "180, 180",
                "157.5, 180",
                "170, 180",
                "-180, 180",
                "-157.5, 180",
                "-170, 180"
            })
            void testSouth(double input, double expected) {
                assertEquals(expected, OrientationCommon.roundYaw(input), 0.001);
            }

            @ParameterizedTest
            @DisplayName("should round to West (-90°)")
            @CsvSource({
                "-90, -90",
                "-67.5, -90",
                "-112.4, -90",
                "-80, -90"
            })
            void testWest(double input, double expected) {
                assertEquals(expected, OrientationCommon.roundYaw(input), 0.001);
            }
        }

        @Nested
        @DisplayName("Intercardinal directions")
        class IntercardinalTests {

            @ParameterizedTest
            @DisplayName("should round to Northeast (45°)")
            @CsvSource({
                "45, 45",
                "22.5, 45",
                "67.4, 45",
                "35, 45"
            })
            void testNortheast(double input, double expected) {
                assertEquals(expected, OrientationCommon.roundYaw(input), 0.001);
            }

            @ParameterizedTest
            @DisplayName("should round to Southeast (135°)")
            @CsvSource({
                "135, 135",
                "112.5, 135",
                "157.4, 135",
                "125, 135"
            })
            void testSoutheast(double input, double expected) {
                assertEquals(expected, OrientationCommon.roundYaw(input), 0.001);
            }

            @ParameterizedTest
            @DisplayName("should round to Southwest (-135°)")
            @CsvSource({
                "-135, -135",
                "-112.5, -135",
                "-157.4, -135",
                "-125, -135"
            })
            void testSouthwest(double input, double expected) {
                assertEquals(expected, OrientationCommon.roundYaw(input), 0.001);
            }

            @ParameterizedTest
            @DisplayName("should round to Northwest (-45°)")
            @CsvSource({
                "-45, -45",
                "-22.5, -45",
                "-67.4, -45",
                "-35, -45"
            })
            void testNorthwest(double input, double expected) {
                assertEquals(expected, OrientationCommon.roundYaw(input), 0.001);
            }
        }
    }

    @Nested
    @DisplayName("roundYaw defensive fallback")
    class RoundYawFallbackTests {

        @ParameterizedTest
        @DisplayName("should pass through unchanged when called directly with an out-of-range value")
        @CsvSource({
            "200, 200",
            "-200, -200",
            "181, 181"
        })
        void testOutOfRangeFallsThrough(double input, double expected) {
            // roundYaw's doc says its input "should be normalized to -180 to 180" via
            // normalizeHeadYaw first, but it's a public method that can be called directly with an
            // out-of-range value (e.g. from future callers who skip normalization). None of the ten
            // explicit range checks matches such a value, so it falls through to the final
            // "return yaw;" - this test exercises that defensive path directly.
            assertEquals(expected, OrientationCommon.roundYaw(input), 0.001);
        }
    }

    @Nested
    @DisplayName("snapYaw")
    class SnapYawTests {

        @ParameterizedTest
        @DisplayName("should normalize and round in a single call")
        @CsvSource({
            "450, 90",      // 450 -> 90 -> 90 (East)
            "405, 45",      // 405 -> 45 -> 45 (NE)
            "-450, -90",    // -450 -> -90 -> -90 (West)
            "10, 0",        // in-range, rounds to North
            "170, 180"      // in-range, rounds to South
        })
        void testSnapYaw(double input, float expected) {
            assertEquals(expected, OrientationCommon.snapYaw(input), 0.001f);
        }
    }

    @Nested
    @DisplayName("init")
    class InitTests {

        @Test
        @DisplayName("should be a no-op that can be called without throwing")
        void testInitDoesNotThrow() {
            OrientationCommon.init();
        }
    }

    @Nested
    @DisplayName("Integration: normalize then round")
    class IntegrationTests {

        @ParameterizedTest
        @DisplayName("should correctly normalize and round large angles")
        @CsvSource({
            "450, 90",      // 450 -> 90 -> 90 (East)
            "405, 45",      // 405 -> 45 -> 45 (NE)
            "-450, -90",    // -450 -> -90 -> -90 (West)
            "-405, -45",    // -405 -> -45 -> -45 (NW)
            "730, 0",       // 730 -> 10 -> 0 (North)
            "750, 45"       // 750 -> 30 -> 45 (NE)
        })
        void testNormalizeAndRound(double input, double expected) {
            double normalized = OrientationCommon.normalizeHeadYaw(input);
            double rounded = OrientationCommon.roundYaw(normalized);
            assertEquals(expected, rounded, 0.001);
        }
    }
}
