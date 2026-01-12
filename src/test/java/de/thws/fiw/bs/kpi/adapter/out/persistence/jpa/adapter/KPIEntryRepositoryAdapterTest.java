package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.adapter;

import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import de.thws.fiw.bs.kpi.application.domain.exception.InfrastructureException;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntryId;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
class KPIEntryRepositoryAdapterTest {

    @Inject
    KPIEntryRepositoryAdapter adapter;

    private final PageRequest defaultPage = new PageRequest(1, 10);
    private static final Clock clock = Clock.fixed(Instant.parse("2026-01-07T10:00:00Z"), ZoneOffset.UTC);


    KPIAssignmentId assignment1 = KPIAssignmentId.newId();
    KPIAssignmentId assignment2 = KPIAssignmentId.newId();
    Instant timestamp1 = Instant.parse("2026-01-06T12:00:00Z");
    Instant timestamp2 = Instant.parse("2026-01-05T10:00:00Z");

    private void createDefaultEntry() {
        adapter.save(new KPIEntry(KPIEntryId.newId(), assignment1, timestamp1, 20.0, clock));
        adapter.save(new KPIEntry(KPIEntryId.newId(), assignment1, timestamp2, 18.0, clock));
        adapter.save(new KPIEntry(KPIEntryId.newId(), assignment2, timestamp1, 30.0, clock));
    }

    @Test
    void findById_idExists_returnsEntry() {
        KPIEntryId id = KPIEntryId.newId();
        KPIEntry entry = new KPIEntry(id, assignment1, timestamp1, 42.0, clock);

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
        createDefaultEntry();

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
    void findByFilter_filtersAreNull_returnsAll() {
        createDefaultEntry();

        Page<KPIEntry> result = adapter.findByFilter(null, null, null, defaultPage);

        assertEquals(3, result.content().size());
        assertEquals(3, result.totalElements());

        List<Double> values = result.content().stream()
                .map(KPIEntry::getValue).toList();

        assertTrue(values.containsAll(List.of(20.0, 18.0, 30.0)));
    }

    @Test
    void findByFilter_onlyAssignmentId_returnsFiltered() {
        createDefaultEntry();

        Page<KPIEntry> result = adapter.findByFilter(assignment1, null, null, defaultPage);

        assertEquals(2, result.content().size());
        assertEquals(2, result.totalElements());

        List<Double> values = result.content().stream()
                .map(KPIEntry::getValue).toList();
        assertTrue(values.containsAll(List.of(20.0, 18.0)));
    }

    @Test
    void findByFilter_onlyFrom_returnsEntriesAfterOrAt() {
        createDefaultEntry();

        Instant from = Instant.parse("2026-01-06T12:00:00Z");

        Page<KPIEntry> result = adapter.findByFilter(null, from, null, defaultPage);

        assertEquals(2, result.totalElements());
        List<Double> values = result.content().stream().map(KPIEntry::getValue).toList();
        assertTrue(values.containsAll(List.of(20.0, 30.0)));
    }

    @Test
    void findByFilter_onlyTo_returnsEntriesBeforeOrAt() {
        createDefaultEntry();

        Instant to = Instant.parse("2026-01-05T10:00:00Z");

        Page<KPIEntry> result = adapter.findByFilter(null, null, to, defaultPage);

        assertEquals(1, result.totalElements());
        assertEquals(18.0, result.content().getFirst().getValue());
    }

    @Test
    void findByFilter_idAndFrom_returnsIntersection() {
        createDefaultEntry();

        Instant from = Instant.parse("2026-01-06T12:00:00Z");

        Page<KPIEntry> result = adapter.findByFilter(assignment1, from, null, defaultPage);

        assertEquals(1, result.totalElements());
        assertEquals(20.0, result.content().getFirst().getValue());
    }

    @Test
    void findByFilter_idAndTo_returnsIntersection() {
        createDefaultEntry();

        Instant to = Instant.parse("2026-01-05T10:00:00Z");

        Page<KPIEntry> result = adapter.findByFilter(assignment1, null, to, defaultPage);

        assertEquals(1, result.totalElements());
        assertEquals(18.0, result.content().getFirst().getValue());
    }

    @Test
    void findByFilter_fromAndTo_returnsRange() {
        createDefaultEntry();

        Instant from = Instant.parse("2026-01-05T10:00:00Z");
        Instant to = Instant.parse("2026-01-05T11:00:00Z");

        Page<KPIEntry> result = adapter.findByFilter(null, from, to, defaultPage);

        assertEquals(1, result.totalElements());
        assertEquals(18.0, result.content().getFirst().getValue());
    }

    @Test
    void findByFilter_allCriteria_returnsSpecificEntry() {
        createDefaultEntry();

        Instant from = Instant.parse("2026-01-06T12:00:00Z");
        Instant to = Instant.parse("2026-01-06T13:00:00Z");

        Page<KPIEntry> result = adapter.findByFilter(assignment1, from, to, defaultPage);

        assertEquals(1, result.totalElements());
        assertEquals(20.0, result.content().getFirst().getValue());
        assertEquals(assignment1, result.content().getFirst().getKpiAssignmentId());
    }

    @Test
    void findByFilter_noMatch_returnsEmptyPage() {
        createDefaultEntry();

        KPIAssignmentId unknownId = KPIAssignmentId.newId();

        Instant from = Instant.parse("2020-01-01T00:00:00Z");
        Instant to = Instant.parse("2020-01-01T23:59:59Z");

        Page<KPIEntry> result = adapter.findByFilter(unknownId, from, to, defaultPage);

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
    }

    @Test
    void findByFilter_secondPage_returnsSecondEntry() {
        createDefaultEntry();

        PageRequest secondPageRequest = new PageRequest(2, 1);

        Page<KPIEntry> result = adapter.findByFilter(null, null, null, secondPageRequest);

        assertEquals(1, result.content().size());
        assertEquals(3, result.totalElements());


        assertNotNull(result.content().getFirst());
    }

    @Test
    void findByFilter_pageOutOfBounds_returnsEmptyPage() {
        createDefaultEntry();

        PageRequest outOfBoundsRequest = new PageRequest(10, 10);

        Page<KPIEntry> result = adapter.findByFilter(null, null, null, outOfBoundsRequest);

        assertTrue(result.content().isEmpty());
        assertEquals(3, result.totalElements());
    }

    @Test
    void save_validEntry_persists() {
        KPIEntryId id = KPIEntryId.newId();
        KPIEntry entry = new KPIEntry(id, assignment1, timestamp1, 40.0, clock);

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
    void save_nullAsKPIEntry_throwException() {
        assertThrows(InfrastructureException.class, () -> adapter.save(null));
    }

    @Test
    void save_duplicateAssignmentAndTimestamp_throwsException() {
        KPIEntry entry = new KPIEntry(KPIEntryId.newId(), assignment1, timestamp1, 20.0, clock);
        adapter.save(entry);

        KPIEntry duplicate = new KPIEntry(KPIEntryId.newId(), assignment1, timestamp1, 30.0, clock);

        assertThrows(AlreadyExistsException.class, () -> adapter.save(duplicate));
    }

    @Test
    void update_validChanges_updatesValue() {
        KPIEntryId id = KPIEntryId.newId();
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        Instant timestamp = Instant.now(clock);

        adapter.save(new KPIEntry(id, assignmentId, timestamp, 1.0, clock));

        KPIEntry updated = new KPIEntry(id, assignmentId, timestamp, 99.0, clock);
        adapter.update(updated);

        Optional<KPIEntry> result = adapter.findById(id);
        assertTrue(result.isPresent());
        assertEquals(99.0, result.get().getValue());
    }

    @Test
    void update_nonExistentEntry_throwsException() {
        KPIEntry ghostEntry = new KPIEntry(KPIEntryId.newId(), assignment1, timestamp1, 99.0, clock);

        assertThrows(InfrastructureException.class, () -> adapter.update(ghostEntry));
    }

    @Test
    void update_nullAsEntry_throwsException() {
        assertThrows(InfrastructureException.class, () -> adapter.update(null));
    }

    @Test
    void delete_idExists_removesEntry() {
        KPIEntryId id = KPIEntryId.newId();
        adapter.save(new KPIEntry(id, assignment1, timestamp1, 40.0, clock));

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
        adapter.save(new KPIEntry(existingId, assignment1, timestamp1, 10.0, clock));

        KPIEntryId nonExistentId = KPIEntryId.newId();
        adapter.delete(nonExistentId);

        assertTrue(adapter.findById(existingId).isPresent());

        Page<KPIEntry> all = adapter.findByFilter(null, null, null, defaultPage);
        assertEquals(1, all.totalElements());
    }

}