package de.thws.fiw.bs.kpi.application.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThresholdsTest {

    @Test
    void rejectsNullTargetDestination() {
        assertThrows(
                NullPointerException.class,
                () -> Thresholds.forDestination(null, 100, 70, 40)
        );
    }

    @Test
    void createsThresholdsForIncreasingDestination() {
        Thresholds thresholds = Thresholds.forDestination(TargetDestination.INCREASING, 100, 70, 40);

        assertEquals(100, thresholds.getGreen());
        assertEquals(70, thresholds.getYellow());
        assertEquals(40, thresholds.getRed());
    }

    @Test
    void rejectsInvalidThresholdsForIncreasingDestination() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.forDestination(TargetDestination.INCREASING, 50, 70, 40)
        );
    }

    @Test
    void createsThresholdsForDecreasingDestination() {
        Thresholds thresholds = Thresholds.forDestination(TargetDestination.DECREASING, 40, 70, 100);

        assertEquals(40, thresholds.getGreen());
        assertEquals(70, thresholds.getYellow());
        assertEquals(100, thresholds.getRed());
    }

    @Test
    void rejectsInvalidThresholdsForDecreasingDestination() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.forDestination(TargetDestination.DECREASING, 50, 70, 40)
        );
    }

    @Test
    void createsThresholdsForRangeDestination() {
        Thresholds thresholds = Thresholds.forDestination(TargetDestination.RANGE, 50, 0.5, 2.3);

        assertEquals(50, thresholds.getGreen());
        assertEquals(0.5, thresholds.getYellow());
        assertEquals(2.3, thresholds.getRed());
    }

    @Test
    void rejectsInvalidThresholdsForRangeDestination() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.forDestination(TargetDestination.RANGE, 25, 1.5, 0.5)
        );
    }

    @Test
    void rejectsNaNThresholdValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.forDestination(TargetDestination.INCREASING, Double.NaN, 70, 40)
        );
    }
}