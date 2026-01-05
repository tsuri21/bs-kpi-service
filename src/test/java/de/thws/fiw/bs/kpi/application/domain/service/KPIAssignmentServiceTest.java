package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.KPIAssignmentCommand;
import de.thws.fiw.bs.kpi.application.port.out.KPIAssignmentRepository;
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
    KPIService kpiService;

    @InjectMock
    ProjectService projectService;

    @Test
    void readById_idGiven_callsRepository() {
        KPIAssignmentId id = KPIAssignmentId.newId();

        kpiAssignmentService.readById(id);

        verify(kpiAssignmentRepository).findById(id);
    }

    @Test
    void readAll_filtersGiven_callsRepository() {
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();
        PageRequest pageRequest = new PageRequest(1, 10);

        kpiAssignmentService.readAll(kpiId, projectId, pageRequest);

        verify(kpiAssignmentRepository).findByFilter(kpiId, projectId, pageRequest);
    }

    @Test
    void create_everythingExists_savesAssignmentWithExpectedFields() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignmentCommand cmd = createKpiAssignmentCmdMock(assignmentId, kpiId, projectId);

        KPI kpi = mock(KPI.class);
        when(kpi.getDestination()).thenReturn(TargetDestination.INCREASING);
        when(kpiService.readById(kpiId)).thenReturn(Optional.of(kpi));

        Project project = mock(Project.class);
        when(projectService.readById(projectId)).thenReturn(Optional.of(project));

        ArgumentCaptor<KPIAssignment> captor = ArgumentCaptor.forClass(KPIAssignment.class);

        kpiAssignmentService.create(cmd);

        verify(kpiAssignmentRepository).save(captor.capture());
        KPIAssignment saved = captor.getValue();

        assertNotNull(saved);
        assertEquals(assignmentId, saved.getId());
        assertEquals(projectId, saved.getProjectId());
        assertSame(kpi, saved.getKpi());
        assertNotNull(saved.getThresholds());
        assertEquals(3.0, saved.getThresholds().getGreen());
        assertEquals(2.0, saved.getThresholds().getYellow());
        assertEquals(1.0, saved.getThresholds().getRed());
    }

    @Test
    void create_kpiDoesNotExist_throwsExceptionAndDoesNotSave() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignmentCommand cmd = createKpiAssignmentCmdMock(assignmentId, kpiId, projectId);

        when(kpiService.readById(kpiId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiAssignmentService.create(cmd));
        verify(kpiAssignmentRepository, never()).save(any());
        verify(projectService, never()).readById(any());
    }

    @Test
    void create_projectDoesNotExist_throwsExceptionAndDoesNotSave() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignmentCommand cmd = createKpiAssignmentCmdMock(assignmentId, kpiId, projectId);

        KPI kpi = mock(KPI.class);
        when(kpi.getDestination()).thenReturn(TargetDestination.INCREASING);
        when(kpiService.readById(kpiId)).thenReturn(Optional.of(kpi));

        when(projectService.readById(projectId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiAssignmentService.create(cmd));
        verify(kpiAssignmentRepository, never()).save(any());
    }

    @Test
    void update_everythingExists_updatesAssignmentWithExpectedFields() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignmentCommand cmd = createKpiAssignmentCmdMock(assignmentId, kpiId, projectId);

        KPI kpi = mock(KPI.class);
        when(kpi.getDestination()).thenReturn(TargetDestination.INCREASING);
        when(kpiService.readById(kpiId)).thenReturn(Optional.of(kpi));

        Project project = mock(Project.class);
        when(projectService.readById(projectId)).thenReturn(Optional.of(project));

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
        assertEquals(3.0, updated.getThresholds().getGreen());
        assertEquals(2.0, updated.getThresholds().getYellow());
        assertEquals(1.0, updated.getThresholds().getRed());
    }

    @Test
    void update_assignmentDoesNotExist_throwsExceptionAndDoesNotUpdate() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignmentCommand cmd = createKpiAssignmentCmdMock(assignmentId, kpiId, projectId);

        KPI kpi = mock(KPI.class);
        when(kpi.getDestination()).thenReturn(TargetDestination.INCREASING);
        when(kpiService.readById(kpiId)).thenReturn(Optional.of(kpi));

        Project project = mock(Project.class);
        when(projectService.readById(projectId)).thenReturn(Optional.of(project));

        when(kpiAssignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiAssignmentService.update(cmd));
        verify(kpiAssignmentRepository, never()).update(any());
    }

    @Test
    void update_kpiDoesNotExist_throwsExceptionAndDoesNotUpdate() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignmentCommand cmd = createKpiAssignmentCmdMock(assignmentId, kpiId, projectId);

        when(kpiService.readById(kpiId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiAssignmentService.update(cmd));
        verify(kpiAssignmentRepository, never()).update(any());
        verify(projectService, never()).readById(any());
        verify(kpiAssignmentRepository, never()).findById(any());
    }

    @Test
    void update_projectDoesNotExist_throwsExceptionAndDoesNotUpdate() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        KPIAssignmentCommand cmd = createKpiAssignmentCmdMock(assignmentId, kpiId, projectId);

        KPI kpi = mock(KPI.class);
        when(kpi.getDestination()).thenReturn(TargetDestination.INCREASING);
        when(kpiService.readById(kpiId)).thenReturn(Optional.of(kpi));

        when(projectService.readById(projectId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiAssignmentService.update(cmd));
        verify(kpiAssignmentRepository, never()).update(any());
        verify(kpiAssignmentRepository, never()).findById(any());
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

    private KPIAssignmentCommand createKpiAssignmentCmdMock(KPIAssignmentId id, KPIId kpiId, ProjectId projectId) {
        KPIAssignmentCommand cmd = mock(KPIAssignmentCommand.class);
        when(cmd.id()).thenReturn(id);
        when(cmd.kpiId()).thenReturn(kpiId);
        when(cmd.projectId()).thenReturn(projectId);
        when(cmd.green()).thenReturn(3.0);
        when(cmd.yellow()).thenReturn(2.0);
        when(cmd.red()).thenReturn(1.0);
        return cmd;
    }
}