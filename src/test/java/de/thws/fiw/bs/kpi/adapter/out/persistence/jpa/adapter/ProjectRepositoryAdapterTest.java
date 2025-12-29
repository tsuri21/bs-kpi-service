package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.adapter;

import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import de.thws.fiw.bs.kpi.application.domain.exception.InfrastructureException;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.Project;
import de.thws.fiw.bs.kpi.application.domain.model.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.RepoUrl;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
class ProjectRepositoryAdapterTest {

    @Inject
    ProjectRepositoryAdapter adapter;

    private final PageRequest defaultPage = new PageRequest(1, 10);

    private void createDefaultProjects() {
        adapter.save(new Project(ProjectId.newId(), new Name("Alpha"), RepoUrl.parse("https://github.com/org/alpha")));
        adapter.save(new Project(ProjectId.newId(), new Name("Beta"), RepoUrl.parse("https://github.com/org/beta")));
        adapter.save(new Project(ProjectId.newId(), new Name("Gamma"), RepoUrl.parse("https://github.com/org/gamma")));
    }

    @Test
    void findById_idExists_returnsProject() {
        ProjectId projectId = ProjectId.newId();
        Project project = new Project(projectId, new Name("Project"), RepoUrl.parse("https://github.com/org/repo"));

        adapter.save(project);

        Optional<Project> result = adapter.findById(projectId);
        assertTrue(result.isPresent());

        Project loaded = result.get();
        assertEquals(project.getId(), loaded.getId());
        assertEquals(project.getName(), loaded.getName());
        assertEquals(project.getRepoUrl(), loaded.getRepoUrl());
    }

    @Test
    void findById_idMissing_returnsEmpty() {
        ProjectId nonExistentId = ProjectId.newId();
        Optional<Project> result = adapter.findById(nonExistentId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByFilter_filtersAreNull_returnsAll() {
        createDefaultProjects();

        Page<Project> result = adapter.findByFilter(null, null, defaultPage);

        assertEquals(3, result.content().size());
        assertEquals(3, result.totalElements());

        List<String> names = result.content().stream()
                .map(p -> p.getName().value())
                .toList();

        assertTrue(names.containsAll(List.of("Alpha", "Beta", "Gamma")));
    }

    @Test
    void findByFilter_onlyName_returnsFiltered() {
        createDefaultProjects();

        Page<Project> result = adapter.findByFilter(new Name("Alpha"), null, defaultPage);

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertEquals("Alpha", result.content().getFirst().getName().value());
    }

    @Test
    void findByFilter_onlyUrl_returnsFiltered() {
        createDefaultProjects();

        RepoUrl targetUrl = RepoUrl.parse("https://github.com/org/beta");
        Page<Project> result = adapter.findByFilter(null, targetUrl, defaultPage);

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertEquals("Beta", result.content().getFirst().getName().value());
    }

    @Test
    void findByFilter_bothCriteria_returnsIntersection() {
        createDefaultProjects();

        Name nameAlpha = new Name("Alpha");
        RepoUrl urlAlpha = RepoUrl.parse("https://github.com/org/alpha");
        RepoUrl urlBeta = RepoUrl.parse("https://github.com/org/beta");

        Page<Project> result = adapter.findByFilter(nameAlpha, urlAlpha, defaultPage);
        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertEquals("Alpha", result.content().getFirst().getName().value());

        Page<Project> resultEmpty = adapter.findByFilter(nameAlpha, urlBeta, defaultPage);
        assertTrue(resultEmpty.content().isEmpty());
        assertEquals(0, resultEmpty.totalElements());
    }

    @Test
    void findByFilter_noMatch_returnsEmptyPage() {
        createDefaultProjects();

        Name unknownName = new Name("NonExistent");
        RepoUrl unknownUrl = RepoUrl.parse("https://github.com/unknown/repo");

        Page<Project> result = adapter.findByFilter(unknownName, unknownUrl, defaultPage);

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
    }

    @Test
    void findByFilter_secondPage_returnsSecondProject() {
        createDefaultProjects();

        PageRequest secondPageRequest = new PageRequest(2, 1);

        Page<Project> result = adapter.findByFilter(null, null, secondPageRequest);

        assertEquals(1, result.content().size());
        assertEquals(3, result.totalElements());
        assertEquals("Beta", result.content().getFirst().getName().value());
    }

    @Test
    void findByFilter_pageOutOfBounds_returnsEmptyPage() {
        createDefaultProjects();

        PageRequest outOfBoundsRequest = new PageRequest(10, 10);
        Page<Project> result = adapter.findByFilter(null, null, outOfBoundsRequest);

        assertTrue(result.content().isEmpty());
        assertEquals(3, result.totalElements());
    }

    @Test
    void save_validProject_persists() {
        ProjectId id = ProjectId.newId();
        Project project = new Project(id, new Name("Test"), RepoUrl.parse("https://github.com/org/test"));

        adapter.save(project);

        Optional<Project> entity = adapter.findById(id);
        assertTrue(entity.isPresent());

        Project loaded = entity.get();
        assertEquals(id, loaded.getId());
        assertEquals("Test", loaded.getName().value());
        assertEquals("https://github.com/org/test", loaded.getRepoUrl().toString());

        Page<Project> allProjects = adapter.findByFilter(null, null, defaultPage);
        assertEquals(1, allProjects.totalElements());
    }

    @Test
    void save_nullAsProject_throwsException() {
        assertThrows(InfrastructureException.class, () -> adapter.save(null));
    }

    @Test
    void save_duplicateName_throwsException() {
        Name sharedName = new Name("UniqueProject");
        adapter.save(new Project(ProjectId.newId(), sharedName, RepoUrl.parse("https://github.com/org/p1")));

        Project duplicate = new Project(ProjectId.newId(), sharedName, RepoUrl.parse("https://github.com/org/p2"));

        assertThrows(AlreadyExistsException.class, () -> adapter.save(duplicate));
    }

    @Test
    void save_duplicateUrl_throwsException() {
        RepoUrl sharedUrl = RepoUrl.parse("https://github.com/org/duplicate");
        adapter.save(new Project(ProjectId.newId(), new Name("First"), sharedUrl));

        Project duplicate = new Project(ProjectId.newId(), new Name("Second"), sharedUrl);

        assertThrows(AlreadyExistsException.class, () -> adapter.save(duplicate));
    }

    @Test
    void update_validChanges_updatesProject() {
        ProjectId id = ProjectId.newId();
        Project initialProject = new Project(id, new Name("Old Name"), RepoUrl.parse("https://github.com/org/old"));
        adapter.save(initialProject);

        Project updatedProject = new Project(id, new Name("New Name"), RepoUrl.parse("https://github.com/org/new"));
        adapter.update(updatedProject);

        Optional<Project> result = adapter.findById(id);
        assertTrue(result.isPresent());

        Project loaded = result.get();
        assertEquals(id, loaded.getId());
        assertEquals("New Name", loaded.getName().value());
        assertEquals("https://github.com/org/new", loaded.getRepoUrl().toString());
    }

    @Test
    void update_nonExistentProject_throwsException() {
        Project project = new Project(ProjectId.newId(), new Name("Ghost"), RepoUrl.parse("https://github.com/org/ghost"));

        assertThrows(InfrastructureException.class, () -> adapter.update(project));
    }

    @Test
    void update_nullAsProject_throwsException() {
        assertThrows(InfrastructureException.class, () -> adapter.update(null));
    }

    @Test
    void delete_idExists_removesProject() {
        ProjectId id = ProjectId.newId();
        adapter.save(new Project(id, new Name("To be deleted"), RepoUrl.parse("https://github.com/org/test")));

        assertTrue(adapter.findById(id).isPresent());

        adapter.delete(id);

        Optional<Project> result = adapter.findById(id);
        assertTrue(result.isEmpty(), "Project should be gone after deletion");

        Page<Project> all = adapter.findByFilter(null, null, defaultPage);
        assertEquals(0, all.totalElements());
    }

    @Test
    void delete_idMissing_doesNothing() {
        ProjectId existingId = ProjectId.newId();
        adapter.save(new Project(existingId, new Name("Some Project"), RepoUrl.parse("https://github.com/org/test")));

        ProjectId nonExistentId = ProjectId.newId();
        adapter.delete(nonExistentId);

        assertTrue(adapter.findById(existingId).isPresent());

        Page<Project> all = adapter.findByFilter(null, null, new PageRequest(1, 10));
        assertEquals(1, all.totalElements());
    }
}