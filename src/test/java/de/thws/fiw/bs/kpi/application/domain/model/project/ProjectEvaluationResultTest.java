package de.thws.fiw.bs.kpi.application.domain.model.project;

import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.Status;
import de.thws.fiw.bs.kpi.application.domain.model.Thresholds;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIEvaluationResult;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectEvaluationResultTest {

    private Project project;
    private KPIAssignment assignment;
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2025-01-10T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        project = new Project(ProjectId.newId(), new Name("Test Project"), RepoUrl.parse("https://github.com/test/repo"));
        KPI kpi = new KPI(KPIId.newId(), new Name("Test KPI"), TargetDestination.INCREASING);
        Thresholds thresholds = Thresholds.linear(TargetDestination.INCREASING, 10.0, 5.0);
        assignment = new KPIAssignment(KPIAssignmentId.newId(), thresholds, kpi, ProjectId.newId());
    }

    private KPIEvaluationResult createResult(double value, String timeIso) {
        KPIEntry entry = KPIEntry.createNew(
                KPIEntryId.newId(),
                assignment.getId(),
                Instant.parse(timeIso),
                value,
                FIXED_CLOCK
        );
        return KPIEvaluationResult.evaluate(assignment, entry);
    }

    @Test
    void aggregate_anyArgumentNull_throwsException() {
        assertThrows(NullPointerException.class, () -> ProjectEvaluationResult.aggregate(null, List.of()));
        assertThrows(NullPointerException.class, () -> ProjectEvaluationResult.aggregate(project, null));
    }

    @Test
    void aggregate_emptyList_throwsException() {
        List<KPIEvaluationResult> emptyList = Collections.emptyList();
        assertThrows(IllegalArgumentException.class, () -> ProjectEvaluationResult.aggregate(project, emptyList));
    }

    @Test
    void aggregate_differentStatus_picksWorstStatusAsFocus() {
        KPIEvaluationResult green = createResult(12.0, "2025-01-01T10:00:00Z");
        KPIEvaluationResult red = createResult(2.0, "2025-01-01T11:00:00Z");
        KPIEvaluationResult yellow = createResult(7.0, "2025-01-01T12:00:00Z");

        List<KPIEvaluationResult> list = List.of(green, red, yellow);

        ProjectEvaluationResult result = ProjectEvaluationResult.aggregate(project, list);

        assertEquals(Status.RED, result.getStatus());
        assertEquals(red, result.getFocusKpi());
        assertTrue(list.containsAll(result.getAllKpis()));
    }

    @Test
    void aggregate_sameStatus_picksNewestAsFocus() {
        KPIEvaluationResult oldRed = createResult(1.0, "2025-01-01T10:00:00Z");
        KPIEvaluationResult newRed = createResult(1.0, "2025-01-05T10:00:00Z"); // Neuer!

        List<KPIEvaluationResult> list = List.of(oldRed, newRed);

        ProjectEvaluationResult result = ProjectEvaluationResult.aggregate(project, list);

        assertEquals(newRed, result.getFocusKpi());
        assertEquals(Status.RED, result.getStatus());
        assertTrue(list.containsAll(result.getAllKpis()));
    }

    @Test
    void aggregate_differentStatus_sortsListByRelevance() {
        KPIEvaluationResult green = createResult(20.0, "2025-01-01T10:00:00Z");
        KPIEvaluationResult red = createResult(1.0, "2025-01-01T10:00:00Z");
        KPIEvaluationResult yellow = createResult(6.0, "2025-01-01T10:00:00Z");

        List<KPIEvaluationResult> input = List.of(green, red, yellow);

        ProjectEvaluationResult result = ProjectEvaluationResult.aggregate(project, input);
        List<KPIEvaluationResult> sorted = result.getAllKpis();

        assertEquals(Status.RED, sorted.get(0).getStatus());
        assertEquals(Status.YELLOW, sorted.get(1).getStatus());
        assertEquals(Status.GREEN, sorted.get(2).getStatus());
    }

    @Test
    void immutability_check() {
        KPIEvaluationResult res = createResult(10.0, "2025-01-01T10:00:00Z");
        ProjectEvaluationResult projectResult = ProjectEvaluationResult.aggregate(project, List.of(res));

        assertThrows(UnsupportedOperationException.class, () -> projectResult.getAllKpis().add(res));
    }

}