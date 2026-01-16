package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.project.Project;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.port.in.KPIAssignmentCommand;
import de.thws.fiw.bs.kpi.application.port.out.KPIAssignmentRepository;
import de.thws.fiw.bs.kpi.application.port.out.KPIRepository;
import de.thws.fiw.bs.kpi.application.port.out.ProjectRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class KPIAssignmentServiceTest {

    @Inject
    KPIAssignmentService kpiAssignmentService;

    @InjectMock
    KPIAssignmentRepository kpiAssignmentRepository;

    @InjectMock
    KPIRepository kpiRepository;

    @InjectMock
    ProjectRepository projectRepository;

    private KPIAssignmentCommand createKpiAssignmentCmdForIncreasing(KPIAssignmentId id, KPIId kpiId, ProjectId projectId) {
        return new KPIAssignmentCommand(id, kpiId, projectId, 15.0, 10.0, null);
    }

    private KPIAssignmentCommand createKpiAssignmentCmdForRange(KPIAssignmentId id, KPIId kpiId, ProjectId projectId) {
        return new KPIAssignmentCommand(id, kpiId, projectId, 10.0, 20.0, 5.0);
    }

    @Test
    void create_everythingExists_savesAssignmentWithExpectedFields() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignmentCommand cmd = createKpiAssignmentCmdForIncreasing(assignmentId, kpiId, projectId);

        KPI kpi = new KPI(kpiId, new Name("Test"), TargetDestination.INCREASING);
        when(kpiRepository.findById(kpiId)).thenReturn(Optional.of(kpi));

        Project project = mock(Project.class);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        ArgumentCaptor<KPIAssignment> captor = ArgumentCaptor.forClass(KPIAssignment.class);

        kpiAssignmentService.create(cmd);

        verify(kpiAssignmentRepository).save(captor.capture());
        KPIAssignment saved = captor.getValue();

        assertNotNull(saved);
        assertEquals(assignmentId, saved.getId());
        assertEquals(projectId, saved.getProjectId());
        assertSame(kpi, saved.getKpi());
        assertNotNull(saved.getThresholds());
        assertEquals(15.0, saved.getThresholds().getGreen());
        assertEquals(10.0, saved.getThresholds().getYellow());
    }

    @Test
    void create_kpiDoesNotExist_throwsExceptionAndDoesNotSave() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignmentCommand cmd = createKpiAssignmentCmdForIncreasing(assignmentId, kpiId, projectId);

        when(kpiRepository.findById(kpiId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiAssignmentService.create(cmd));
        verify(kpiAssignmentRepository, never()).save(any());
    }

    @Test
    void create_projectDoesNotExist_throwsExceptionAndDoesNotSave() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignmentCommand cmd = createKpiAssignmentCmdForIncreasing(assignmentId, kpiId, projectId);

        KPI kpi = new KPI(kpiId, new Name("Test"), TargetDestination.INCREASING);
        when(kpiRepository.findById(kpiId)).thenReturn(Optional.of(kpi));

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiAssignmentService.create(cmd));
        verify(kpiAssignmentRepository, never()).save(any());
    }

    @Test
    void update_everythingExists_updatesAssignmentWithExpectedFields() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignmentCommand cmd = createKpiAssignmentCmdForRange(assignmentId, kpiId, projectId);

        KPI kpi = new KPI(kpiId, new Name("Test"), TargetDestination.RANGE);
        when(kpiRepository.findById(kpiId)).thenReturn(Optional.of(kpi));

        Project project = mock(Project.class);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        KPIAssignment kpiAssignment = mock(KPIAssignment.class);
        when(kpiAssignmentRepository.findById(assignmentId)).thenReturn(Optional.of(kpiAssignment));

        ArgumentCaptor<KPIAssignment> captor = ArgumentCaptor.forClass(KPIAssignment.class);

        kpiAssignmentService.update(cmd);

        verify(kpiAssignmentRepository).update(captor.capture());
        KPIAssignment updated = captor.getValue();

        assertNotNull(updated);
        assertEquals(assignmentId, updated.getId());
        assertEquals(projectId, updated.getProjectId());
        assertSame(kpi, updated.getKpi());
        assertNotNull(updated.getThresholds());
        assertEquals(10.0, updated.getThresholds().getGreen());
        assertEquals(20.0, updated.getThresholds().getYellow());
        assertEquals(5.0, updated.getThresholds().getTargetValue());
    }

    @Test
    void update_assignmentDoesNotExist_throwsExceptionAndDoesNotUpdate() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignmentCommand cmd = createKpiAssignmentCmdForRange(assignmentId, kpiId, projectId);
        when(kpiAssignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiAssignmentService.update(cmd));
        verify(kpiAssignmentRepository, never()).update(any());
    }

    @Test
    void update_kpiDoesNotExist_throwsExceptionAndDoesNotUpdate() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignmentCommand cmd = createKpiAssignmentCmdForRange(assignmentId, kpiId, projectId);

        when(kpiRepository.findById(kpiId)).thenReturn(Optional.empty());
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiAssignmentService.update(cmd));
        verify(kpiAssignmentRepository, never()).update(any());
    }

    @Test
    void update_projectDoesNotExist_throwsExceptionAndDoesNotUpdate() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignmentCommand cmd = createKpiAssignmentCmdForRange(assignmentId, kpiId, projectId);

        KPI kpi = new KPI(kpiId, new Name("Test"), TargetDestination.RANGE);
        when(kpiRepository.findById(kpiId)).thenReturn(Optional.of(kpi));

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiAssignmentService.update(cmd));
        verify(kpiAssignmentRepository, never()).update(any());
    }

    @Test
    void delete_assignmentExists_callsRepository() {
        KPIAssignmentId id = KPIAssignmentId.newId();

        KPIAssignment assignment = mock(KPIAssignment.class);
        when(kpiAssignmentRepository.findById(id)).thenReturn(Optional.of(assignment));

        kpiAssignmentService.delete(id);

        verify(kpiAssignmentRepository).delete(id);
    }

    @Test
    void delete_assignmentDoesNotExist_throwsException() {
        KPIAssignmentId id = KPIAssignmentId.newId();

        when(kpiAssignmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiAssignmentService.delete(id));
        verify(kpiAssignmentRepository, never()).delete(any());
    }
}