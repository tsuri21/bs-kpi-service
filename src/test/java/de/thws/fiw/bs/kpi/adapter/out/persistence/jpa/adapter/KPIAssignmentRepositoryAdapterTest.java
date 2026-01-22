package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.adapter;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.KPIEntity;
import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.ProjectEntity;
import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
class KPIAssignmentRepositoryAdapterTest {

    @Inject
    KPIAssignmentRepositoryAdapter adapter;

    @Inject
    EntityManager em;

    private final PageRequest defaultPage = new PageRequest(1, 10);

    private final KPIAssignmentId defaultKAId1 = KPIAssignmentId.newId();
    private final KPIAssignmentId defaultKAId2 = KPIAssignmentId.newId();
    private final KPIAssignmentId defaultKAId3 = KPIAssignmentId.newId();

    private final KPI defaultK1 = new KPI(KPIId.newId(), new Name("Test1"), TargetDestination.DECREASING);
    private final KPI defaultK2 = new KPI(KPIId.newId(), new Name("Test2"), TargetDestination.DECREASING);
    private final KPI defaultK3 = new KPI(KPIId.newId(), new Name("Test3"), TargetDestination.DECREASING);

    private final ProjectId defaultPId1 = ProjectId.newId();
    private final ProjectId defaultPId2 = ProjectId.newId();
    private final ProjectId defaultPId3 = ProjectId.newId();

    private Thresholds createThresholds() {
        return Thresholds.linear(TargetDestination.DECREASING, 10.0, 20.0);
    }

    private void createKpi(KPI kpi) {
        em.persist(new KPIEntity(kpi.getId().value(), kpi.getName().value(), kpi.getDestination()));
        em.flush();
    }

    private void createProject(ProjectId id, String name) {
        ProjectEntity project = new ProjectEntity(
                id.value(),
                name,
                URI.create("https://github.com/org/" + name)
        );
        em.persist(project);
        em.flush();
    }

    private void createDefaultAssignments() {
        createKpi(defaultK1);
        createKpi(defaultK2);
        createKpi(defaultK3);

        createProject(defaultPId1, "Project-A");
        createProject(defaultPId2, "Project-B");
        createProject(defaultPId3, "Project-C");

        adapter.save(new KPIAssignment(defaultKAId1, createThresholds(), defaultK1, defaultPId1));
        adapter.save(new KPIAssignment(defaultKAId2, createThresholds(), defaultK2, defaultPId2));
        adapter.save(new KPIAssignment(defaultKAId3, createThresholds(), defaultK3, defaultPId3));
    }

    @Test
    void findById_idExists_returnsAssignment() {
        createKpi(defaultK1);

        ProjectId pId = ProjectId.newId();
        createProject(pId, "Test");

        KPIAssignmentId id = KPIAssignmentId.newId();
        KPIAssignment assignment = new KPIAssignment(id, createThresholds(), defaultK1, pId);

        adapter.save(assignment);

        Optional<KPIAssignment> result = adapter.findById(id);
        assertTrue(result.isPresent());

        KPIAssignment loaded = result.get();
        assertEquals(assignment.getId(), loaded.getId());
        assertEquals(assignment.getProjectId(), loaded.getProjectId());
        assertEquals(assignment.getKpi().getId(), loaded.getKpi().getId());
        assertEquals(10.0, result.get().getThresholds().getGreen());
    }

    @Test
    void findById_idMissing_returnsEmpty() {
        KPIAssignmentId nonExisting = KPIAssignmentId.newId();
        Optional<KPIAssignment> result = adapter.findById(nonExisting);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByFilter_filtersAreNull_returnsAll() {
        createDefaultAssignments();

        Page<KPIAssignment> result = adapter.findByFilter(null, null, defaultPage);

        assertEquals(3, result.content().size());
        assertEquals(3, result.totalElements());

        List<Double> values = result.content().stream()
                .map(k -> k.getThresholds().getGreen())
                .toList();

        assertTrue(values.containsAll(List.of(10.0, 10.0, 10.0)));
    }

    @Test
    void findByFilter_onlyKpiId_returnsFiltered() {
        createDefaultAssignments();
        KPI targetKpi = defaultK1;

        Page<KPIAssignment> result = adapter.findByFilter(targetKpi.getId(), null, defaultPage);

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertEquals(defaultKAId1, result.content().getFirst().getId());
    }

    @Test
    void findByFilter_onlyProjectId_returnsFiltered() {
        createDefaultAssignments();
        ProjectId targetProject = defaultPId1;

        Page<KPIAssignment> result = adapter.findByFilter(null, targetProject, defaultPage);

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertEquals(defaultKAId1, result.content().getFirst().getId());
    }

    @Test
    void findByFilter_bothCriteria_returnsIntersection() {
        createDefaultAssignments();
        KPI targetKpi = defaultK1;
        ProjectId targetProject1 = defaultPId1;
        ProjectId targetProject2 = defaultPId2;

        Page<KPIAssignment> result = adapter.findByFilter(targetKpi.getId(), targetProject1, defaultPage);
        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertEquals(defaultKAId1, result.content().getFirst().getId());

        Page<KPIAssignment> resultEmpty = adapter.findByFilter(targetKpi.getId(), targetProject2, defaultPage);
        assertTrue(resultEmpty.content().isEmpty());
    }

    @Test
    void findByFilter_noMatch_returnsEmptyPage() {
        createDefaultAssignments();

        KPIId unknownKPI = KPIId.newId();
        ProjectId unknownProject = ProjectId.newId();

        Page<KPIAssignment> result = adapter.findByFilter(unknownKPI, unknownProject, defaultPage);

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
    }

    @Test
    void findByFilter_secondPage_returnsCorrectElement() {
        createDefaultAssignments();
        PageRequest secondPageRequest = new PageRequest(2, 1);

        Page<KPIAssignment> result = adapter.findByFilter(null, null, secondPageRequest);

        assertEquals(1, result.content().size());
        assertEquals(3, result.totalElements());
    }

    @Test
    void findByFilter_pageOutOfBounds_returnsEmptyPage() {
        createDefaultAssignments();

        PageRequest outOfBoundsRequest = new PageRequest(10, 10);
        Page<KPIAssignment> result = adapter.findByFilter(null, null, outOfBoundsRequest);

        assertTrue(result.content().isEmpty());
        assertEquals(3, result.totalElements());
    }

    @Test
    void save_validAssignment_persists() {
        KPIAssignmentId id = KPIAssignmentId.newId();
        createKpi(defaultK1);
        KPI kpi = defaultK1;

        ProjectId projectId = ProjectId.newId();
        createProject(projectId, "Test");

        KPIAssignment assignment = new KPIAssignment(id, createThresholds(), kpi, projectId);

        adapter.save(assignment);

        Optional<KPIAssignment> result = adapter.findById(id);
        assertTrue(result.isPresent());

        KPIAssignment loaded = result.get();
        assertEquals(id, loaded.getId());
        assertEquals(kpi.getId(), loaded.getKpi().getId());
        assertEquals(projectId, loaded.getProjectId());

        Page<KPIAssignment> allAssignments = adapter.findByFilter(null, null, defaultPage);
        assertEquals(1, allAssignments.totalElements());
    }

    @Test
    void save_duplicateKPIIdAndProjectId_throwsException() {
        createKpi(defaultK1);
        KPI kpi = defaultK1;

        ProjectId projectId = ProjectId.newId();
        createProject(projectId, "Test");

        adapter.save(new KPIAssignment(KPIAssignmentId.newId(), createThresholds(), kpi, projectId));

        KPIAssignment duplicate = new KPIAssignment(KPIAssignmentId.newId(), createThresholds(), kpi, projectId);

        assertThrows(AlreadyExistsException.class, () -> adapter.save(duplicate));
    }

    @Test
    void update_validChanges_updatesKPIAssignment() {
        KPIAssignmentId id = KPIAssignmentId.newId();
        createKpi(defaultK1);
        KPI kpi = defaultK1;

        ProjectId project = ProjectId.newId();
        createProject(project, "Test");

        adapter.save(new KPIAssignment(id, createThresholds(), kpi, project));

        Thresholds newThresholds = Thresholds.linear(TargetDestination.DECREASING, 79, 89);
        adapter.update(new KPIAssignment(id, newThresholds, kpi, project));

        Optional<KPIAssignment> result = adapter.findById(id);
        assertTrue(result.isPresent());
        assertEquals(79.0, result.get().getThresholds().getGreen());
        assertEquals(89.0, result.get().getThresholds().getYellow());
    }

    @Test
    void delete_idExists_removesAssignment() {
        createKpi(defaultK1);
        KPIAssignmentId id = KPIAssignmentId.newId();

        ProjectId pId = ProjectId.newId();
        createProject(pId, "Test");

        adapter.save(new KPIAssignment(id, createThresholds(), defaultK1, pId));

        adapter.delete(id);

        assertTrue(adapter.findById(id).isEmpty());
    }

    @Test
    void delete_idMissing_doesNothing() {
        createKpi(defaultK1);
        KPIAssignmentId existingId = KPIAssignmentId.newId();

        ProjectId pId = ProjectId.newId();
        createProject(pId, "Test");

        adapter.save(new KPIAssignment(existingId, createThresholds(), defaultK1, pId));

        adapter.delete(KPIAssignmentId.newId());

        assertTrue(adapter.findById(existingId).isPresent());
    }
}