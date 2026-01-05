package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.Project;
import de.thws.fiw.bs.kpi.application.domain.model.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.RepoUrl;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.out.ProjectRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class ProjectServiceTest {

    @Inject
    ProjectService projectService;

    @InjectMock
    ProjectRepository projectRepository;

    @Test
    void readById_idGiven_callsRepository() {
        ProjectId id = ProjectId.newId();

        projectService.readById(id);

        verify(projectRepository).findById(id);
    }

    @Test
    void readAll_filtersGiven_callsRepository() {
        Name name = new Name("Test");
        RepoUrl url = RepoUrl.parse("https://github.com/org/test");
        PageRequest pageRequest = new PageRequest(1, 10);

        projectService.readAll(name, url, pageRequest);

        verify(projectRepository).findByFilter(name, url, pageRequest);
    }

    @Test
    void create_projectGiven_callsRepository() {
        Project project = mock(Project.class);

        projectService.create(project);

        verify(projectRepository).save(project);
    }

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