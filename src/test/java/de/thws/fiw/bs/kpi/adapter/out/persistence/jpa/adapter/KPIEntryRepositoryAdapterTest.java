package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.adapter;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.KPIAssignmentEntity;
import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.KPIEntity;
import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntryId;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
class KPIEntryRepositoryAdapterTest {

    @Inject
    KPIEntryRepositoryAdapter adapter;

    @Inject
    EntityManager em;

    private final PageRequest defaultPage = new PageRequest(1, 10);
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-07T10:00:00Z"), ZoneOffset.UTC);

    KPIAssignmentId assignment1 = KPIAssignmentId.newId();
    KPIAssignmentId assignment2 = KPIAssignmentId.newId();
    Instant timestamp1 = Instant.parse("2026-01-06T12:00:00Z");
    Instant timestamp2 = Instant.parse("2026-01-05T10:00:00Z");

    private void createDefaultEntries() {
        adapter.save(KPIEntry.createNew(KPIEntryId.newId(), assignment1, timestamp1, 20.0, FIXED_CLOCK));
        adapter.save(KPIEntry.createNew(KPIEntryId.newId(), assignment1, timestamp2, 18.0, FIXED_CLOCK));
        adapter.save(KPIEntry.createNew(KPIEntryId.newId(), assignment2, timestamp1, 30.0, FIXED_CLOCK));
    }

    private void createAssignmentForProject(KPIAssignmentId assignmentId, ProjectId projectId, String kpiName) {
        KPIEntity kpi = new KPIEntity(UUID.randomUUID(), kpiName, TargetDestination.DECREASING);
        em.persist(kpi);

        KPIAssignmentEntity entity = new KPIAssignmentEntity(
                assignmentId.value(),
                5.0,
                10.0,
                15.0,
                kpi,
                projectId.value()
        );

        em.persist(entity);
        em.flush();
    }

    @Test
    void findById_idExists_returnsEntry() {
        KPIEntryId id = KPIEntryId.newId();
        KPIEntry entry = KPIEntry.createNew(id, assignment1, timestamp1, 42.0, FIXED_CLOCK);

        adapter.save(entry);

        Optional<KPIEntry> result = adapter.findById(id);
        assertTrue(result.isPresent());

        KPIEntry found = result.get();
        assertEquals(id, found.getId());
        assertEquals(42.0, result.get().getValue());
        assertEquals(timestamp1, found.getTimestamp());
    }

    @Test
    void findById_idMissing_returnsEmpty() {
        KPIEntryId id = KPIEntryId.newId();
        Optional<KPIEntry> result = adapter.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void findLatest_assignmentExists_returnsMostRecentEntry() {
        createDefaultEntries();

        Optional<KPIEntry> latest = adapter.findLatest(assignment1);

        assertTrue(latest.isPresent());
        assertEquals(20.0, latest.get().getValue());
        assertEquals(timestamp1, latest.get().getTimestamp());
    }

    @Test
    void findLatest_assignmentMissing_returnsEmpty() {
        Optional<KPIEntry> latest = adapter.findLatest(assignment2);

        assertTrue(latest.isEmpty());
    }

    @Test
    void findLatestEntriesByProject_multipleAssignmentsWithHistory_returnsMapWithOnlyLatestEntries() {
        ProjectId projectId = ProjectId.newId();
        KPIAssignmentId assignmentIdA = KPIAssignmentId.newId();
        KPIAssignmentId assignmentIdB = KPIAssignmentId.newId();
        createAssignmentForProject(assignmentIdA, projectId, "Test1");
        createAssignmentForProject(assignmentIdB, projectId, "Test2");

        adapter.save(KPIEntry.createNew(KPIEntryId.newId(), assignmentIdA, timestamp2, 18.0, FIXED_CLOCK));
        adapter.save(KPIEntry.createNew(KPIEntryId.newId(), assignmentIdA, timestamp1, 20.0, FIXED_CLOCK));

        adapter.save(KPIEntry.createNew(KPIEntryId.newId(), assignmentIdB, timestamp1, 30.0, FIXED_CLOCK));

        Map<KPIAssignmentId, KPIEntry> result = adapter.findLatestEntriesByProject(projectId);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue(result.containsKey(assignmentIdA));
        assertEquals(20.0, result.get(assignmentIdA).getValue());
        assertEquals(timestamp1, result.get(assignmentIdA).getTimestamp());

        assertTrue(result.containsKey(assignmentIdB));
        assertEquals(30.0, result.get(assignmentIdB).getValue());
    }

    @Test
    void findLatestEntriesByProject_entriesFromOtherProjects_areIgnored() {
        ProjectId projectIdA = ProjectId.newId();
        KPIAssignmentId assignmentIdA = KPIAssignmentId.newId();
        createAssignmentForProject(assignmentIdA, projectIdA, "Test1");
        adapter.save(KPIEntry.createNew(KPIEntryId.newId(), assignmentIdA, timestamp1, 100.0, FIXED_CLOCK));

        ProjectId projectIdB = ProjectId.newId();
        KPIAssignmentId assignmentIdB = KPIAssignmentId.newId();
        createAssignmentForProject(assignmentIdB, projectIdB, "Test2");
        adapter.save(KPIEntry.createNew(KPIEntryId.newId(), assignmentIdB, timestamp1, 999.0, FIXED_CLOCK));

        Map<KPIAssignmentId, KPIEntry> result = adapter.findLatestEntriesByProject(projectIdA);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(assignmentIdA));
        assertFalse(result.containsKey(assignmentIdB));
        assertEquals(100.0, result.get(assignmentIdA).getValue());
    }

    @Test
    void findLatestEntriesByProject_noAssignmentsOrEntries_returnsEmptyMap() {
        ProjectId projectId = ProjectId.newId();

        Map<KPIAssignmentId, KPIEntry> result = adapter.findLatestEntriesByProject(projectId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByFilter_filtersAreNull_returnsAll() {
        createDefaultEntries();

        Page<KPIEntry> result = adapter.findByFilter(null, null, null, defaultPage);

        assertEquals(3, result.content().size());
        assertEquals(3, result.totalElements());

        List<Double> values = result.content().stream()
                .map(KPIEntry::getValue).toList();

        assertTrue(values.containsAll(List.of(20.0, 18.0, 30.0)));
    }

    @Test
    void findByFilter_onlyAssignmentId_returnsFiltered() {
        createDefaultEntries();

        Page<KPIEntry> result = adapter.findByFilter(assignment1, null, null, defaultPage);

        assertEquals(2, result.content().size());
        assertEquals(2, result.totalElements());

        List<Double> values = result.content().stream()
                .map(KPIEntry::getValue).toList();
        assertTrue(values.containsAll(List.of(20.0, 18.0)));
    }

    @Test
    void findByFilter_onlyFrom_returnsEntriesAfterOrAt() {
        createDefaultEntries();

        Instant from = Instant.parse("2026-01-06T12:00:00Z");

        Page<KPIEntry> result = adapter.findByFilter(null, from, null, defaultPage);

        assertEquals(2, result.totalElements());
        List<Double> values = result.content().stream().map(KPIEntry::getValue).toList();
        assertTrue(values.containsAll(List.of(20.0, 30.0)));
    }

    @Test
    void findByFilter_onlyTo_returnsEntriesBeforeOrAt() {
        createDefaultEntries();

        Instant to = Instant.parse("2026-01-05T10:00:00Z");

        Page<KPIEntry> result = adapter.findByFilter(null, null, to, defaultPage);

        assertEquals(1, result.totalElements());
        assertEquals(18.0, result.content().getFirst().getValue());
    }

    @Test
    void findByFilter_idAndFrom_returnsIntersection() {
        createDefaultEntries();

        Instant from = Instant.parse("2026-01-06T12:00:00Z");

        Page<KPIEntry> result = adapter.findByFilter(assignment1, from, null, defaultPage);

        assertEquals(1, result.totalElements());
        assertEquals(20.0, result.content().getFirst().getValue());
    }

    @Test
    void findByFilter_idAndTo_returnsIntersection() {
        createDefaultEntries();

        Instant to = Instant.parse("2026-01-05T10:00:00Z");

        Page<KPIEntry> result = adapter.findByFilter(assignment1, null, to, defaultPage);

        assertEquals(1, result.totalElements());
        assertEquals(18.0, result.content().getFirst().getValue());
    }

    @Test
    void findByFilter_fromAndTo_returnsRange() {
        createDefaultEntries();

        Instant from = Instant.parse("2026-01-05T10:00:00Z");
        Instant to = Instant.parse("2026-01-05T11:00:00Z");

        Page<KPIEntry> result = adapter.findByFilter(null, from, to, defaultPage);

        assertEquals(1, result.totalElements());
        assertEquals(18.0, result.content().getFirst().getValue());
    }

    @Test
    void findByFilter_allCriteria_returnsSpecificEntry() {
        createDefaultEntries();

        Instant from = Instant.parse("2026-01-06T12:00:00Z");
        Instant to = Instant.parse("2026-01-06T13:00:00Z");

        Page<KPIEntry> result = adapter.findByFilter(assignment1, from, to, defaultPage);

        assertEquals(1, result.totalElements());
        assertEquals(20.0, result.content().getFirst().getValue());
        assertEquals(assignment1, result.content().getFirst().getKpiAssignmentId());
    }

    @Test
    void findByFilter_noMatch_returnsEmptyPage() {
        createDefaultEntries();

        KPIAssignmentId unknownId = KPIAssignmentId.newId();

        Instant from = Instant.parse("2020-01-01T00:00:00Z");
        Instant to = Instant.parse("2020-01-01T23:59:59Z");

        Page<KPIEntry> result = adapter.findByFilter(unknownId, from, to, defaultPage);

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
    }

    @Test
    void findByFilter_secondPage_returnsSecondEntry() {
        createDefaultEntries();

        PageRequest secondPageRequest = new PageRequest(2, 1);

        Page<KPIEntry> result = adapter.findByFilter(null, null, null, secondPageRequest);

        assertEquals(1, result.content().size());
        assertEquals(3, result.totalElements());


        assertNotNull(result.content().getFirst());
    }

    @Test
    void findByFilter_pageOutOfBounds_returnsEmptyPage() {
        createDefaultEntries();

        PageRequest outOfBoundsRequest = new PageRequest(10, 10);

        Page<KPIEntry> result = adapter.findByFilter(null, null, null, outOfBoundsRequest);

        assertTrue(result.content().isEmpty());
        assertEquals(3, result.totalElements());
    }

    @Test
    void save_validEntry_persists() {
        KPIEntryId id = KPIEntryId.newId();
        KPIEntry entry = KPIEntry.createNew(id, assignment1, timestamp1, 40.0, FIXED_CLOCK);

        adapter.save(entry);

        Optional<KPIEntry> result = adapter.findById(id);
        assertTrue(result.isPresent());

        KPIEntry found = result.get();
        assertEquals(id, found.getId());
        assertEquals(assignment1, found.getKpiAssignmentId());
        assertEquals(timestamp1, found.getTimestamp());
        assertEquals(40.0, found.getValue());

        Page<KPIEntry> page = adapter.findByFilter(assignment1, null, null, defaultPage);
        assertEquals(1, page.totalElements());
    }

    @Test
    void save_duplicateAssignmentAndTimestamp_throwsException() {
        KPIEntry entry = KPIEntry.createNew(KPIEntryId.newId(), assignment1, timestamp1, 20.0, FIXED_CLOCK);
        adapter.save(entry);

        KPIEntry duplicate = KPIEntry.createNew(KPIEntryId.newId(), assignment1, timestamp1, 30.0, FIXED_CLOCK);

        assertThrows(AlreadyExistsException.class, () -> adapter.save(duplicate));
    }

    @Test
    void delete_idExists_removesEntry() {
        KPIEntryId id = KPIEntryId.newId();
        adapter.save(KPIEntry.createNew(id, assignment1, timestamp1, 40.0, FIXED_CLOCK));

        assertTrue(adapter.findById(id).isPresent());

        adapter.delete(id);

        Optional<KPIEntry> result = adapter.findById(id);
        assertTrue(result.isEmpty(), "KPIEntry should be gone after deletion");

        Page<KPIEntry> page = adapter.findByFilter(null, null, null, defaultPage);
        assertEquals(0, page.totalElements());
    }

    @Test
    void delete_idMissing_doesNothing() {
        KPIEntryId existingId = KPIEntryId.newId();
        adapter.save(KPIEntry.createNew(existingId, assignment1, timestamp1, 10.0, FIXED_CLOCK));

        KPIEntryId nonExistentId = KPIEntryId.newId();
        adapter.delete(nonExistentId);

        assertTrue(adapter.findById(existingId).isPresent());

        Page<KPIEntry> all = adapter.findByFilter(null, null, null, defaultPage);
        assertEquals(1, all.totalElements());
    }

}