package de.thws.fiw.bs.kpi.application.domain.model.kpi;

import static org.junit.jupiter.api.Assertions.*;

import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.Status;
import de.thws.fiw.bs.kpi.application.domain.model.Thresholds;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntryId;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;


class KPIEvaluationResultTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2025-01-01T12:00:00Z"), ZoneOffset.UTC);

    private KPIEntry createDummyEntry(double value) {
        return KPIEntry.createNew(
                KPIEntryId.newId(),
                KPIAssignmentId.newId(),
                Instant.now(FIXED_CLOCK),
                value,
                FIXED_CLOCK
        );
    }

    @Test
    void evaluate_anyArgumentNull_throwsException() {
        KPI kpi = new KPI(KPIId.newId(), new Name("Test"), TargetDestination.INCREASING);
        Thresholds thresholds = Thresholds.linear(TargetDestination.INCREASING, 10.0, 5.0);

        KPIAssignment assignment = new KPIAssignment(KPIAssignmentId.newId(), thresholds, kpi, ProjectId.newId());
        KPIEntry entry = createDummyEntry(10.0);

        assertThrows(NullPointerException.class, () -> KPIEvaluationResult.evaluate(null, entry));
        assertThrows(NullPointerException.class, () -> KPIEvaluationResult.evaluate(assignment, null));
    }

    @Test
    void evaluate_increasingKpi_returnsCorrectStatus() {
        KPI kpi = new KPI(KPIId.newId(), new Name("Test"), TargetDestination.INCREASING);
        Thresholds thresholds = Thresholds.linear(TargetDestination.INCREASING, 10.0, 5.0);

        KPIAssignment assignment = new KPIAssignment(KPIAssignmentId.newId(), thresholds, kpi, ProjectId.newId());
        KPIEntry entry = createDummyEntry(11.0);

        KPIEvaluationResult result = KPIEvaluationResult.evaluate(assignment, entry);

        assertEquals(Status.GREEN, result.getStatus());
        assertEquals(kpi, result.getKpi());
        assertEquals(entry, result.getEntry());
    }

    @Test
    void evaluate_rangeKpi_returnsCorrectStatus() {
        KPI kpi = new KPI(KPIId.newId(), new Name("Test"), TargetDestination.RANGE);
        Thresholds thresholds = Thresholds.range(100.0, 5.0, 10.0);

        KPIAssignment assignment = new KPIAssignment(KPIAssignmentId.newId(), thresholds, kpi, ProjectId.newId());
        KPIEntry entry = createDummyEntry(120.0);

        KPIEvaluationResult result = KPIEvaluationResult.evaluate(assignment, entry);

        assertEquals(Status.RED, result.getStatus());
        assertEquals(kpi, result.getKpi());
        assertEquals(entry, result.getEntry());
    }
}
