package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
import de.thws.fiw.bs.kpi.application.domain.model.project.Project;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.port.out.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @InjectMocks
    ProjectService projectService;

    @Mock
    ProjectRepository projectRepository;

    @Test
    void update_projectExists_callsRepositoryUpdate() {
        ProjectId id = ProjectId.newId();
        Project projectToUpdate = mock(Project.class);

        when(projectToUpdate.getId()).thenReturn(id);
        when(projectRepository.findById(id)).thenReturn(Optional.of(projectToUpdate));

        projectService.update(projectToUpdate);
        verify(projectRepository).update(projectToUpdate);
    }

    @Test
    void update_projectDoesNotExist_throwsException() {
        ProjectId id = ProjectId.newId();
        Project projectToUpdate = mock(Project.class);

        when(projectToUpdate.getId()).thenReturn(id);
        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.update(projectToUpdate));
        verify(projectRepository, never()).update(any());
    }

    @Test
    void delete_projectExists_callsRepository() {
        ProjectId id = ProjectId.newId();
        Project mockProject = mock(Project.class);

        when(projectRepository.findById(id)).thenReturn(Optional.of(mockProject));

        projectService.delete(id);
        verify(projectRepository).delete(id);
    }

    @Test
    void delete_projectDoesNotExist_throwsException() {
        ProjectId id = ProjectId.newId();

        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.delete(id));
        verify(projectRepository, never()).delete(any());
    }
}