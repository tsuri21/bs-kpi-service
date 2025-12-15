package de.thws.fiw.bs.kpi.application.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KPIAssignmentTest {

    private final UUID assignmentId = UUID.randomUUID();
    private final UUID kpiId = UUID.randomUUID();

    @Test
    void shouldThrowExceptionWhenKpiIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new KPIAssignment(
                        assignmentId,
                        100,
                        70,
                        40,
                        null,
                        null
                )
        );

        assertEquals("KPI must not be null", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenThresholdsAreSetWithoutKpi() {
        KPIAssignment assignment = new KPIAssignment();
        assignment.setId(assignmentId);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> assignment.setThresholds(100, 70, 40)
        );

        assertEquals("KPI must be set before thresholds", ex.getMessage());
    }

    @Test
    void shouldCreateAssignmentWithValidThresholdsForIncreasingKpi() {
        KPI kpi = new KPI(kpiId, "Demo", TargetDestination.INCREASING);

        KPIAssignment assignment = new KPIAssignment(
                assignmentId,
                100,
                70,
                40,
                kpi,
                null
        );

        assertEquals(100, assignment.getGreen());
        assertEquals(70, assignment.getYellow());
        assertEquals(40, assignment.getRed());
    }

    @Test
    void shouldThrowExceptionWhenThresholdsAreInvalidForIncreasingKpi() {
        KPI kpi = new KPI(kpiId, "Demo", TargetDestination.INCREASING);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new KPIAssignment(
                        assignmentId,
                        50,
                        70,
                        40,
                        kpi,
                        null
                )
        );

        assertEquals("Thresholds are not valid", ex.getMessage());
    }

    @Test
    void shouldCreateAssignmentWithValidThresholdsForDecreasingKpi() {
        KPI kpi = new KPI(kpiId, "Demo", TargetDestination.DECREASING);

        KPIAssignment assignment = new KPIAssignment(
                assignmentId,
                40,
                70,
                100,
                kpi,
                null
        );

        assertEquals(40, assignment.getGreen());
        assertEquals(70, assignment.getYellow());
        assertEquals(100, assignment.getRed());
    }

    @Test
    void shouldThrowExceptionWhenThresholdsAreInvalidForDecreasingKpi() {
        KPI kpi = new KPI(kpiId, "Demo", TargetDestination.DECREASING);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new KPIAssignment(
                        assignmentId,
                        50,
                        70,
                        40,
                        kpi,
                        null
                )
        );

        assertEquals("Thresholds are not valid", ex.getMessage());
    }

    @Test
    void shouldCreateAssignmentWithValidThresholdsForRangeKpi() {
        KPI kpi = new KPI(kpiId, "Demo", TargetDestination.RANGE);

        KPIAssignment assignment = new KPIAssignment(
                assignmentId,
                50,
                0.5,
                2.3,
                kpi,
                null
        );

        assertEquals(50, assignment.getGreen());
        assertEquals(0.5, assignment.getYellow());
        assertEquals(2.3, assignment.getRed());
    }

    @Test
    void shouldThrowExceptionWhenThresholdsAreInvalidForRangeKpi() {
        KPI kpi = new KPI(kpiId, "Demo", TargetDestination.RANGE);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new KPIAssignment(
                        assignmentId,
                        25,
                        1.5,
                        0.5,
                        kpi,
                        null
                )
        );

        assertEquals("Thresholds are not valid", ex.getMessage());
    }
}
