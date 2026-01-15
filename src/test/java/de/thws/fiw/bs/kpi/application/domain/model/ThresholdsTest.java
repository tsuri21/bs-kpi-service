package de.thws.fiw.bs.kpi.application.domain.model;

import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThresholdsTest {

    @Test
    void init_nanValue_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.linear(TargetDestination.INCREASING, Double.NaN, 70)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.linear(TargetDestination.INCREASING, 50, Double.NaN)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.range(Double.NaN, 1.4, 2.0)
        );
    }

    @Test
    void linear_nullDestination_throwsException() {
        assertThrows(
                NullPointerException.class,
                () -> Thresholds.linear(null, 100, 70)
        );
    }

    @Test
    void linear_rangeDestination_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.linear(TargetDestination.RANGE, 100, 70)
        );
    }

    @Test
    void linear_increasingValid_success() {
        Thresholds thresholds = Thresholds.linear(TargetDestination.INCREASING, 100, 70);

        assertEquals(100, thresholds.getGreen());
        assertEquals(70, thresholds.getYellow());
    }

    @Test
    void linear_increasingOutOfOrder_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.linear(TargetDestination.INCREASING, 50, 70)
        );
    }

    @Test
    void linear_decreasingValid_success() {
        Thresholds thresholds = Thresholds.linear(TargetDestination.DECREASING, 40, 70);

        assertEquals(40, thresholds.getGreen());
        assertEquals(70, thresholds.getYellow());
    }

    @Test
    void linear_decreasingOutOfOrder_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.linear(TargetDestination.DECREASING, 70, 50)
        );
    }

    @Test
    void range_rangeValid_success() {
        Thresholds thresholds = Thresholds.range(50, 0.5, 2.3);

        assertEquals(50, thresholds.getTargetValue());
        assertEquals(0.5, thresholds.getGreen());
        assertEquals(2.3, thresholds.getYellow());
    }

    @Test
    void range_rangeOutOfOrder_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.range(25, 1.5, 0.5)
        );
    }

    @Test
    void range_targetValueZero_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Thresholds.range(0.0, 1.5, 2.4)
        );
    }
}