package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.adapter;

import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import de.thws.fiw.bs.kpi.application.domain.exception.InfrastructureException;
import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
class KPIAssignmentRepositoryAdapterTest {

    @Inject
    KPIAssignmentRepositoryAdapter adapter;

    private final PageRequest defaultPage = new PageRequest(1, 10);

    private final KPIAssignmentId defaultKAId1 = KPIAssignmentId.newId();
    private final KPIAssignmentId defaultKAId2 = KPIAssignmentId.newId();
    private final KPIAssignmentId defaultKAId3 = KPIAssignmentId.newId();

    private final KPIId defaultKId1 = KPIId.newId();
    private final KPIId defaultKId2 = KPIId.newId();
    private final KPIId defaultKId3 = KPIId.newId();

    private final ProjectId defaultPId1 = ProjectId.newId();
    private final ProjectId defaultPId2 = ProjectId.newId();
    private final ProjectId defaultPId3 = ProjectId.newId();

    private Thresholds createThresholds() {
        return Thresholds.forDestination(TargetDestination.DECREASING, 10.0, 20.0, 30.0);
    }

    private void createDefaultAssignments() {
        adapter.save(new KPIAssignment(defaultKAId1, createThresholds(), defaultKId1, defaultPId1));
        adapter.save(new KPIAssignment(defaultKAId2, createThresholds(), defaultKId2, defaultPId2));
        adapter.save(new KPIAssignment(defaultKAId3, createThresholds(), defaultKId3, defaultPId3));
    }

    @Test
    void findById_idExists_returnsAssignment() {
        KPIAssignmentId id =KPIAssignmentId.newId();
        KPIAssignment assignment = new KPIAssignment(id, createThresholds(),KPIId.newId(), ProjectId.newId());

        adapter.save(assignment);

        Optional<KPIAssignment> result = adapter.findById(id);
        assertTrue(result.isPresent());

        KPIAssignment loaded = result.get();
        assertEquals(assignment.getId(), loaded.getId());
        assertEquals(assignment.getProjectId(), loaded.getProjectId());
        assertEquals(assignment.getKpiId(), loaded.getKpiId());
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

        assertEquals(List.of(10.0, 10.0, 10.0),  values);
    }

    @Test
    void findByFilter_onlyKpiId_returnsFiltered() {
        KPIId targetKpi = defaultKId1;
        createDefaultAssignments();

        Page<KPIAssignment> result = adapter.findByFilter(targetKpi, null, defaultPage);

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertEquals(defaultKAId1, result.content().getFirst().getId());
    }

    @Test
    void findByFilter_onlyProjectId_returnsFiltered() {
        ProjectId targetProject = defaultPId1;
        createDefaultAssignments();

        Page<KPIAssignment> result = adapter.findByFilter(null, targetProject, defaultPage);

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertEquals(defaultKAId1, result.content().getFirst().getId());
    }

    @Test
    void findByFilter_bothCriteria_returnsIntersection() {
        KPIId targetKpi = defaultKId1;
        ProjectId targetProject1 = defaultPId1;
        ProjectId targetProject2 = defaultPId2;
        createDefaultAssignments();

        Page<KPIAssignment> result = adapter.findByFilter(targetKpi, targetProject1, defaultPage);
        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertEquals(defaultKAId1, result.content().getFirst().getId());

        Page<KPIAssignment> resultEmpty = adapter.findByFilter(targetKpi, targetProject2, defaultPage);
        assertTrue(resultEmpty.content().isEmpty());
    }

    @Test
    void findByFilter_noMatch_returnsEmptyPage(){
        createDefaultAssignments();

        KPIId unknownKPI = KPIId.newId();
        ProjectId unkownProject = ProjectId.newId();

        Page<KPIAssignment> result = adapter.findByFilter(unknownKPI, unkownProject, defaultPage);

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
    void findByFilter_pageOutOfBounds_returnsEmptyPage(){
        createDefaultAssignments();

        PageRequest outOfBoundsRequest = new PageRequest(10, 10);
        Page<KPIAssignment> result = adapter.findByFilter(null, null, outOfBoundsRequest);

        assertTrue(result.content().isEmpty());
        assertEquals(3, result.totalElements());
    }

    @Test
    void save_validAssignment_persists() {
        KPIAssignmentId id = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();
        KPIAssignment assignment = new KPIAssignment(id, createThresholds(), kpiId, projectId);

        adapter.save(assignment);

        Optional<KPIAssignment> result = adapter.findById(id);
        assertTrue(result.isPresent());

        KPIAssignment loaded = result.get();
        assertEquals(id, loaded.getId());
        assertEquals(kpiId, loaded.getKpiId());
        assertEquals(projectId, loaded.getProjectId());

        Page<KPIAssignment> allAssignments = adapter.findByFilter(null, null, defaultPage);
        assertEquals(1, allAssignments.totalElements());
    }

    @Test
    void save_nullAsKPIAssignments_throwsException() {
        assertThrows(InfrastructureException.class, () -> adapter.save(null));
    }

    @Test
    void save_duplicateKPIIdAndProjectId_throwsException() {
        KPIId kpid = KPIId.newId();
        ProjectId projectId = ProjectId.newId();
        adapter.save(new KPIAssignment(KPIAssignmentId.newId(), createThresholds(), kpid, projectId));

        KPIAssignment duplicate = new KPIAssignment(KPIAssignmentId.newId(), createThresholds(), kpid, projectId);

        assertThrows(AlreadyExistsException.class, () -> adapter.save(duplicate));
    }
    @Test
    void update_validChanges_updatesKPIAssignment() {
        KPIAssignmentId id = KPIAssignmentId.newId();
        KPIId kpi = KPIId.newId();
        ProjectId project = ProjectId.newId();

        adapter.save(new KPIAssignment(id, createThresholds(), kpi, project));

        Thresholds newThresholds = Thresholds.forDestination(TargetDestination.INCREASING, 99, 89, 79);
        adapter.update(new KPIAssignment(id, newThresholds, kpi, project));

        Optional<KPIAssignment> result = adapter.findById(id);
        assertTrue(result.isPresent());
        assertEquals(99.0, result.get().getThresholds().getGreen());
        assertEquals(89.0, result.get().getThresholds().getYellow());
        assertEquals(79.0, result.get().getThresholds().getRed());
    }

    @Test
    void update_nonExistent_throwsException() {
        KPIAssignment ghost = new KPIAssignment(KPIAssignmentId.newId(), createThresholds(), KPIId.newId(), ProjectId.newId());
        assertThrows(InfrastructureException.class, () -> adapter.update(ghost));
    }

    @Test
    void update_nullAsKPIAssignment(){
        assertThrows(InfrastructureException.class, () -> adapter.update(null));
    }

    @Test
    void delete_idExists_removesAssignment() {
        KPIAssignmentId id = KPIAssignmentId.newId();
        adapter.save(new KPIAssignment(id, createThresholds(), KPIId.newId(), ProjectId.newId()));

        adapter.delete(id);

        assertTrue(adapter.findById(id).isEmpty());
    }

    @Test
    void delete_idMissing_doesNothing() {
        KPIAssignmentId existingId = KPIAssignmentId.newId();
        adapter.save(new KPIAssignment(existingId, createThresholds(), KPIId.newId(), ProjectId.newId()));

        adapter.delete(KPIAssignmentId.newId());

        assertTrue(adapter.findById(existingId).isPresent());
    }




}