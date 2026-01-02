package de.thws.fiw.bs.kpi.application.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThresholdsTest {

    @Test
    void forDestination_nullDestination_throwsException() {
        assertThrows(
                NullPointerException.class,
                () -> Thresholds.forDestination(null, 100, 70, 40)
        );
    }

    @Test
    void forDestination_nanValue_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.forDestination(TargetDestination.INCREASING, Double.NaN, 70, 40)
        );
    }

    @Test
    void forDestination_increasingValid_success() {
        Thresholds thresholds = Thresholds.forDestination(TargetDestination.INCREASING, 100, 70, 40);

        assertEquals(100, thresholds.getGreen());
        assertEquals(70, thresholds.getYellow());
        assertEquals(40, thresholds.getRed());
    }

    @Test
    void forDestination_increasingOutOfOrder_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.forDestination(TargetDestination.INCREASING, 50, 70, 40)
        );
    }

    @Test
    void forDestination_decreasingValid_success() {
        Thresholds thresholds = Thresholds.forDestination(TargetDestination.DECREASING, 40, 70, 100);

        assertEquals(40, thresholds.getGreen());
        assertEquals(70, thresholds.getYellow());
        assertEquals(100, thresholds.getRed());
    }

    @Test
    void forDestination_decreasingOutOfOrder_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.forDestination(TargetDestination.DECREASING, 50, 70, 40)
        );
    }

    @Test
    void forDestination_rangeValid_success() {
        Thresholds thresholds = Thresholds.forDestination(TargetDestination.RANGE, 50, 0.5, 2.3);

        assertEquals(50, thresholds.getGreen());
        assertEquals(0.5, thresholds.getYellow());
        assertEquals(2.3, thresholds.getRed());
    }

    @Test
    void forDestination_rangeOutOfOrder_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.forDestination(TargetDestination.RANGE, 25, 1.5, 0.5)
        );
    }
}