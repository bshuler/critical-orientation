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
