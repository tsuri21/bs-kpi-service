package de.thws.fiw.bs.kpi.application.domain.model.kpi;

import static org.junit.jupiter.api.Assertions.*;

import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.Status;
import de.thws.fiw.bs.kpi.application.domain.model.Thresholds;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntryId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;


class EvaluatedKPITest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2025-10-01T10:00:00Z"), ZoneOffset.UTC);

    private KPI createKPI(TargetDestination destination) {
        return new KPI(KPIId.newId(), new Name("Test"), destination);
    }

    private Thresholds createThresholdsForIncreasing() {
        return Thresholds.forDestination(TargetDestination.INCREASING, 3.0, 2.0, 1.0);
    }

    private Thresholds createThresholdsForDecreasing() {
        return Thresholds.forDestination(TargetDestination.DECREASING, 1.0, 2.0, 3.0);
    }

    private Thresholds createThresholdsForRange() {
        return Thresholds.forDestination(TargetDestination.RANGE, 10.0, 1.2, 1.5);
    }

    @Test
    void evaluateKPI_anyArgumentNull_throwsException() {
        KPIEntry latest = KPIEntry.createNew(KPIEntryId.newId(), KPIAssignmentId.newId(), Instant.parse("2025-05-01T11:00:00Z"), 4.0, FIXED_CLOCK);
        KPI kpi = createKPI(TargetDestination.INCREASING);
        Thresholds thresholds = createThresholdsForIncreasing();

        assertThrows(NullPointerException.class, () -> EvaluatedKPI.evaluateKPI(null, thresholds, latest));
        assertThrows(NullPointerException.class, () -> EvaluatedKPI.evaluateKPI(kpi, null, latest));
        assertThrows(NullPointerException.class, () -> EvaluatedKPI.evaluateKPI(kpi, thresholds, null));
    }

    @Test
    void evaluateKPI_destinationIncreasing_returnGreen() {
        KPIEntry latest = KPIEntry.createNew(KPIEntryId.newId(), KPIAssignmentId.newId(), Instant.parse("2025-05-01T11:00:00Z"), 4.0, FIXED_CLOCK);
        KPI kpi = createKPI(TargetDestination.INCREASING);
        Thresholds thresholds = createThresholdsForIncreasing();

        assertEquals(Status.GREEN, EvaluatedKPI.evaluateKPI(kpi, thresholds, latest).getStatus());
    }

    @Test
    void evaluateKPI_destinationIncreasing_returnYellow() {
        KPIEntry latest = KPIEntry.createNew(KPIEntryId.newId(), KPIAssignmentId.newId(), Instant.parse("2025-05-01T11:00:00Z"), 2.5, FIXED_CLOCK);
        KPI kpi = createKPI(TargetDestination.INCREASING);
        Thresholds thresholds = createThresholdsForIncreasing();

        assertEquals(Status.YELLOW, EvaluatedKPI.evaluateKPI(kpi, thresholds, latest).getStatus());
    }

    @Test
    void evaluateKPI_destinationIncreasing_returnRed() {
        KPIEntry latest = KPIEntry.createNew(KPIEntryId.newId(), KPIAssignmentId.newId(), Instant.parse("2025-05-01T11:00:00Z"), 1.5, FIXED_CLOCK);
        KPI kpi = createKPI(TargetDestination.INCREASING);
        Thresholds thresholds = createThresholdsForIncreasing();

        assertEquals(Status.RED, EvaluatedKPI.evaluateKPI(kpi, thresholds, latest).getStatus());
    }

    @Test
    void evaluateKPI_destinationDecreasing_returnGreen() {
        KPIEntry latest = KPIEntry.createNew(KPIEntryId.newId(), KPIAssignmentId.newId(), Instant.parse("2025-05-01T11:00:00Z"), 0.5, FIXED_CLOCK);
        KPI kpi = createKPI(TargetDestination.DECREASING);
        Thresholds thresholds = createThresholdsForDecreasing();

        assertEquals(Status.GREEN, EvaluatedKPI.evaluateKPI(kpi, thresholds, latest).getStatus());
    }

    @Test
    void evaluateKPI_destinationDecreasing_returnYellow() {
        KPIEntry latest = KPIEntry.createNew(KPIEntryId.newId(), KPIAssignmentId.newId(), Instant.parse("2025-05-01T11:00:00Z"), 1.5, FIXED_CLOCK);
        KPI kpi = createKPI(TargetDestination.DECREASING);
        Thresholds thresholds = createThresholdsForDecreasing();

        assertEquals(Status.YELLOW, EvaluatedKPI.evaluateKPI(kpi, thresholds, latest).getStatus());
    }

    @Test
    void evaluateKPI_destinationDecreasing_returnRed() {
        KPIEntry latest = KPIEntry.createNew(KPIEntryId.newId(), KPIAssignmentId.newId(), Instant.parse("2025-05-01T11:00:00Z"), 2.5, FIXED_CLOCK);
        KPI kpi = createKPI(TargetDestination.DECREASING);
        Thresholds thresholds = createThresholdsForDecreasing();

        assertEquals(Status.RED, EvaluatedKPI.evaluateKPI(kpi, thresholds, latest).getStatus());
    }

    @Test
    void evaluateKPI_destinationRange_returnGreen() {
        KPIEntry latest = KPIEntry.createNew(KPIEntryId.newId(), KPIAssignmentId.newId(), Instant.parse("2025-05-01T11:00:00Z"), 11.0, FIXED_CLOCK);
        KPI kpi = createKPI(TargetDestination.RANGE);
        Thresholds thresholds = createThresholdsForRange();

        assertEquals(Status.GREEN, EvaluatedKPI.evaluateKPI(kpi, thresholds, latest).getStatus());
    }

    @Test
    void evaluateKPI_destinationRange_returnYellow() {
        KPIEntry latest = KPIEntry.createNew(KPIEntryId.newId(), KPIAssignmentId.newId(), Instant.parse("2025-05-01T11:00:00Z"), 24.0, FIXED_CLOCK);
        KPI kpi = createKPI(TargetDestination.RANGE);
        Thresholds thresholds = createThresholdsForRange();

        assertEquals(Status.YELLOW, EvaluatedKPI.evaluateKPI(kpi, thresholds, latest).getStatus());
    }

    @Test
    void evaluateKPI_destinationRange_returnRed() {
        KPIEntry latest = KPIEntry.createNew(KPIEntryId.newId(), KPIAssignmentId.newId(), Instant.parse("2025-05-01T11:00:00Z"), 27.0, FIXED_CLOCK);
        KPI kpi = createKPI(TargetDestination.RANGE);
        Thresholds thresholds = createThresholdsForRange();

        assertEquals(Status.RED, EvaluatedKPI.evaluateKPI(kpi, thresholds, latest).getStatus());
    }
}
