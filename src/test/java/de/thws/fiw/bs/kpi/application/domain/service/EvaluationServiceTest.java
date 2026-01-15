package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.Status;
import de.thws.fiw.bs.kpi.application.domain.model.Thresholds;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.EvaluatedKPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntryId;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.port.out.KPIAssignmentRepository;
import de.thws.fiw.bs.kpi.application.port.out.KPIEntryRepository;
import de.thws.fiw.bs.kpi.application.port.out.ProjectRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class EvaluationServiceTest {

    @Inject
    EvaluationService evaluationService;

    @InjectMock
    KPIAssignmentRepository kpiAssignmentRepository;

    @InjectMock
    KPIEntryRepository kpiEntryRepository;

    @InjectMock
    ProjectRepository projectRepository;

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2025-01-10T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void evaluateKPI_kpiAssignmentExists_returnEvaluatedKPI(){
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        KPIAssignment kpiAssignment = new KPIAssignment(
                assignmentId,
                Thresholds.forDestination(TargetDestination.DECREASING, 1.0, 3.0, 5.0),
                new KPI(kpiId, new Name("Test"), TargetDestination.DECREASING),
                ProjectId.newId()
        );

        KPIEntryId entryId = KPIEntryId.newId();
        KPIEntry entry = new KPIEntry(entryId, assignmentId, Instant.parse("2025-01-01T11:00:00Z"), 10.5, FIXED_CLOCK);

        when(kpiAssignmentRepository.findById(assignmentId)).thenReturn(Optional.of(kpiAssignment));
        when(kpiEntryRepository.findLatest(assignmentId)).thenReturn(Optional.of(entry));

        EvaluatedKPI evaluatedKPI = evaluationService.evaluateKPI(assignmentId);
        assertEquals(kpiId, evaluatedKPI.getId());
        assertEquals(Status.RED, evaluatedKPI.getStatus());
    }

    //@Test
    //void evaluateKPI_KPIAssignmentDoesNotExist

}