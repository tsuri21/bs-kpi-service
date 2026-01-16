package de.thws.fiw.bs.kpi.application.domain.model;

import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThresholdsTest {

    private Thresholds createIncreasing() {
        return Thresholds.linear(TargetDestination.INCREASING, 3.0, 2.0);
    }

    private Thresholds createDecreasing() {
        return Thresholds.linear(TargetDestination.DECREASING, 1.0, 2.0);
    }

    private Thresholds createRange() {
        return Thresholds.range(10.0, 1.2, 1.5);
    }

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
    void calculateStatus_destinationNull_throwsException() {
        Thresholds t = createIncreasing();
        assertThrows(NullPointerException.class, () -> t.calculateStatus(5.0, null));
    }

    @Test
    void calculateStatus_increasing_valueAboveGreenLimit_returnsGreen() {
        Thresholds t = createIncreasing();
        assertEquals(Status.GREEN, t.calculateStatus(4.0, TargetDestination.INCREASING));
    }

    @Test
    void calculateStatus_increasing_valueBetweenLimits_returnsYellow() {
        Thresholds t = createIncreasing();
        assertEquals(Status.YELLOW, t.calculateStatus(2.5, TargetDestination.INCREASING));
    }

    @Test
    void calculateStatus_increasing_valueBelowYellowLimit_returnsRed() {
        Thresholds t = createIncreasing();
        assertEquals(Status.RED, t.calculateStatus(1.5, TargetDestination.INCREASING));
    }

    @Test
    void calculateStatus_decreasing_valueBelowGreenLimit_returnsGreen() {
        Thresholds t = createDecreasing();
        assertEquals(Status.GREEN, t.calculateStatus(0.5, TargetDestination.DECREASING));
    }

    @Test
    void calculateStatus_decreasing_valueBetweenLimits_returnsYellow() {
        Thresholds t = createDecreasing();
        assertEquals(Status.YELLOW, t.calculateStatus(1.5, TargetDestination.DECREASING));
    }

    @Test
    void calculateStatus_decreasing_valueAboveYellowLimit_returnsRed() {
        Thresholds t = createDecreasing();
        assertEquals(Status.RED, t.calculateStatus(2.5, TargetDestination.DECREASING));
    }

    @Test
    void calculateStatus_range_withinGreenTolerance_returnsGreen() {
        Thresholds t = createRange();

        assertEquals(Status.GREEN, t.calculateStatus(11.0, TargetDestination.RANGE));
        assertEquals(Status.GREEN, t.calculateStatus(9.0, TargetDestination.RANGE));
    }

    @Test
    void calculateStatus_range_outsideGreenButWithinYellow_returnsYellow() {
        Thresholds t = createRange();
        assertEquals(Status.YELLOW, t.calculateStatus(11.4, TargetDestination.RANGE));
    }

    @Test
    void calculateStatus_range_outsideYellowTolerance_returnsRed() {
        Thresholds t = createRange();
        assertEquals(Status.RED, t.calculateStatus(11.6, TargetDestination.RANGE));
    }

    @Test
    void calculateStatus_range_calledWithLinearThresholds_throwsException() {
        Thresholds linear = createIncreasing();
        assertThrows(
                IllegalStateException.class,
                () -> linear.calculateStatus(5.0, TargetDestination.RANGE)
        );
    }
}