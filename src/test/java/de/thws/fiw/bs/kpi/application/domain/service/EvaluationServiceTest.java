package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.EvaluationException;
import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
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
import de.thws.fiw.bs.kpi.application.domain.model.project.Project;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectEvaluationResult;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.project.RepoUrl;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.out.KPIAssignmentRepository;
import de.thws.fiw.bs.kpi.application.port.out.KPIEntryRepository;
import de.thws.fiw.bs.kpi.application.port.out.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @InjectMocks
    EvaluationService evaluationService;

    @Mock
    KPIAssignmentRepository kpiAssignmentRepository;

    @Mock
    KPIEntryRepository kpiEntryRepository;

    @Mock
    ProjectRepository projectRepository;

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2025-01-10T10:00:00Z"), ZoneOffset.UTC);
    private final PageRequest dummyPageRequest = new PageRequest(1, 1000);

    private KPIAssignment createAssignment(KPIAssignmentId id, ProjectId pid, double green, double yellow) {
        KPI kpi = new KPI(KPIId.newId(), new Name("Test KPI"), TargetDestination.INCREASING);
        Thresholds thresholds = Thresholds.linear(TargetDestination.INCREASING, green, yellow);
        return new KPIAssignment(id, thresholds, kpi, pid);
    }

    private KPIEntry createEntry(KPIAssignmentId assignmentId, double value, String timeIso) {
        return KPIEntry.createNew(
                KPIEntryId.newId(),
                assignmentId,
                Instant.parse(timeIso),
                value,
                FIXED_CLOCK
        );
    }

    @Test
    void evaluateKPI_everythingExists_returnsCorrectStatus() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignment assignment = createAssignment(assignmentId, projectId, 10.0, 5.0);

        KPIEntry entry = createEntry(assignmentId, 4.0, "2025-01-01T12:00:00Z");

        when(kpiAssignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(kpiEntryRepository.findLatest(assignmentId)).thenReturn(Optional.of(entry));

        KPIEvaluationResult result = evaluationService.evaluateKPI(assignmentId);

        assertNotNull(result);
        assertEquals(Status.RED, result.getStatus());
        assertEquals(assignment.getKpi().getId(), result.getKpi().getId());
        assertEquals(entry, result.getEntry());
    }

    @Test
    void evaluateKPI_assignmentDoesNotExist_throwsException() {
        KPIAssignmentId id = KPIAssignmentId.newId();
        when(kpiAssignmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> evaluationService.evaluateKPI(id));
    }

    @Test
    void evaluateKPI_entryNotFound_throwsException() {
        KPIAssignmentId id = KPIAssignmentId.newId();
        KPIAssignment assignment = createAssignment(id, ProjectId.newId(), 10, 5);

        when(kpiAssignmentRepository.findById(id)).thenReturn(Optional.of(assignment));
        when(kpiEntryRepository.findLatest(id)).thenReturn(Optional.empty());

        assertThrows(EvaluationException.class, () -> evaluationService.evaluateKPI(id));
    }

    @Test
    void evaluateProject_everythingExists_aggregatesCorrectly() {
        ProjectId projectId = ProjectId.newId();
        Project project = new Project(projectId, new Name("Test Project"), RepoUrl.parse("https://github.com/test/repo"));

        KPIAssignmentId assignment1Id = KPIAssignmentId.newId();
        KPIAssignmentId assignment2Id = KPIAssignmentId.newId();

        KPIAssignment assignment1 = createAssignment(assignment1Id, projectId, 10.0, 5.0);
        KPIAssignment assignment2 = createAssignment(assignment2Id, projectId, 10.0, 5.0);

        KPIEntry entryGreen = createEntry(assignment1Id, 12.0, "2025-01-01T10:00:00Z");
        KPIEntry entryRed = createEntry(assignment2Id, 2.0, "2025-01-01T11:00:00Z");

        Page<KPIAssignment> pageResult = new Page<>(List.of(assignment1, assignment2), dummyPageRequest, 2);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(kpiAssignmentRepository.findByFilter(null, projectId, dummyPageRequest)).thenReturn(pageResult);
        when(kpiEntryRepository.findLatestEntriesByProject(projectId))
                .thenReturn(Map.of(assignment1Id, entryGreen, assignment2Id, entryRed));

        ProjectEvaluationResult result = evaluationService.evaluateProject(projectId);

        assertEquals(Status.RED, result.getStatus());
        assertEquals(project, result.getProject());
        assertEquals(2, result.getAllKpis().size());

        assertEquals(assignment2.getKpi().getId(), result.getFocusKpi().getKpi().getId());
        assertEquals(Status.RED, result.getFocusKpi().getStatus());
    }

    @Test
    void evaluateProject_projectDoesNotExist_throwsException() {
        ProjectId projectId = ProjectId.newId();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> evaluationService.evaluateProject(projectId));
    }

    @Test
    void evaluateProject_tooManyAssignments_throwsException() {
        ProjectId projectId = ProjectId.newId();
        Project project = new Project(projectId, new Name("Large Project"), RepoUrl.parse("https://github.com/test/repo"));

        Page<KPIAssignment> pageResult = new Page<>(List.of(), dummyPageRequest, dummyPageRequest.pageSize() + 1);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(kpiAssignmentRepository.findByFilter(null, projectId, dummyPageRequest)).thenReturn(pageResult);

        assertThrows(EvaluationException.class, () -> evaluationService.evaluateProject(projectId));
    }

    @Test
    void evaluateProject_noAssignments_throwsException() {
        ProjectId projectId = ProjectId.newId();
        Project project = new Project(projectId, new Name("Empty Project"), RepoUrl.parse("https://github.com/test/repo"));

        Page<KPIAssignment> pageResult = new Page<>(List.of(), dummyPageRequest, 0);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(kpiAssignmentRepository.findByFilter(null, projectId, dummyPageRequest)).thenReturn(pageResult);

        assertThrows(EvaluationException.class, () -> evaluationService.evaluateProject(projectId));
    }

    @Test
    void evaluateProject_missingEntryForAssignment_throwsException() {
        ProjectId projectId = ProjectId.newId();
        Project project = new Project(projectId, new Name("Broken Project"), RepoUrl.parse("https://github.com/test/repo"));

        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIAssignment assignment = createAssignment(assignmentId, projectId, 10, 5);

        Page<KPIAssignment> pageResult = new Page<>(List.of(assignment), dummyPageRequest, 1);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(kpiAssignmentRepository.findByFilter(null, projectId, dummyPageRequest)).thenReturn(pageResult);

        when(kpiEntryRepository.findLatestEntriesByProject(projectId)).thenReturn(Map.of());

        Exception ex = assertThrows(EvaluationException.class, () -> evaluationService.evaluateProject(projectId));
        assertTrue(ex.getMessage().contains("has no entries"));
    }
}