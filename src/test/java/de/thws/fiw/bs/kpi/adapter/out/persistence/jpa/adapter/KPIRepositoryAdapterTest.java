package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.adapter;

import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import de.thws.fiw.bs.kpi.application.domain.exception.InfrastructureException;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;
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
class KPIRepositoryAdapterTest {

    @Inject
    KPIRepositoryAdapter adapter;

    private final PageRequest pageRequest = new PageRequest(1, 10);

    private void createDefaultKPIs() {
        adapter.save(new KPI(KPIId.newId(), new Name("Eins"), TargetDestination.DECREASING));
        adapter.save(new KPI(KPIId.newId(), new Name("Zwei"), TargetDestination.INCREASING));
        adapter.save(new KPI(KPIId.newId(), new Name("Drei"), TargetDestination.DECREASING));
    }

    @Test
    void findById_idExists_returnsKPI() {
        KPIId kpiId = KPIId.newId();
        KPI kpi = new KPI(kpiId, new Name("KPI"), TargetDestination.DECREASING);

        adapter.save(kpi);

        Optional<KPI> result = adapter.findById(kpiId);
        assertTrue(result.isPresent());

        KPI loaded = result.get();
        assertEquals(kpi.getId(), loaded.getId());
        assertEquals(kpi.getName(), loaded.getName());
        assertEquals(kpi.getDestination(), loaded.getDestination());
    }

    @Test
    void finById_idMissing_returnsEmpty() {
        KPIId nonExistentId = KPIId.newId();
        Optional<KPI> result = adapter.findById(nonExistentId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByFilter_filterIsNull_returnsAll() {
        createDefaultKPIs();

        Page<KPI> result = adapter.findByFilter(null, pageRequest);

        assertEquals(3, result.content().size());
        assertEquals(3, result.totalElements());

        List<String> names = result.content().stream()
                .map(p -> p.getName().value())
                .toList();

        assertTrue(names.containsAll(List.of("Eins", "Zwei", "Drei")));
    }

    @Test
    void findByFilter_NameIsProvided_returnsFiltered() {
        createDefaultKPIs();

        Page<KPI> result = adapter.findByFilter(new Name("Eins"), pageRequest);
        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertEquals("Eins", result.content().getFirst().getName().value());
    }

    @Test
    void findByFilter_noMatch_returnsEmptyPage() {
        createDefaultKPIs();

        Name unknownName = new Name("Unknown");

        Page<KPI> result = adapter.findByFilter(unknownName, pageRequest);

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
    }

    @Test
    void findByFilter_secondPage_returnsSecondKPI() {
        createDefaultKPIs();

        PageRequest secondPageRequest = new PageRequest(2, 1);

        Page<KPI> result = adapter.findByFilter(null, secondPageRequest);

        assertEquals(1, result.content().size());
        assertEquals(3, result.totalElements());
        assertEquals("Zwei", result.content().getFirst().getName().value());
    }

    @Test
    void findByFilter_pageOutOfBounds_returnsEmptyPage() {
        createDefaultKPIs();

        PageRequest outOfBoundsRequest = new PageRequest(10, 10);
        Page<KPI> result = adapter.findByFilter(null, outOfBoundsRequest);

        assertTrue(result.content().isEmpty());
        assertEquals(3, result.totalElements());
    }

    @Test
    void save_validKPI_persists() {
        KPIId id = KPIId.newId();
        KPI kpi = new KPI(id, new Name("Test"), TargetDestination.DECREASING);

        adapter.save(kpi);

        Optional<KPI> entity = adapter.findById(id);
        assertTrue(entity.isPresent());

        KPI loaded = entity.get();
        assertEquals(id, loaded.getId());
        assertEquals("Test", loaded.getName().value());
        assertEquals(TargetDestination.DECREASING, loaded.getDestination());

        Page<KPI> allKPIs = adapter.findByFilter(null, pageRequest);
        assertEquals(1, allKPIs.totalElements());
    }

    @Test
    void save_nullAsKPI_throwsException() {
        assertThrows(InfrastructureException.class, () -> adapter.save(null));
    }

    @Test
    void save_duplicateName_throwsException() {
        Name sharedName = new Name("UniqueKPI");
        adapter.save(new KPI(KPIId.newId(), sharedName, TargetDestination.DECREASING));

        KPI duplicate = new KPI(KPIId.newId(), sharedName, TargetDestination.INCREASING);

        assertThrows(AlreadyExistsException.class, () -> adapter.save(duplicate));
    }

    @Test
    void update_validChanges_updatesKPI() {
        KPIId id = KPIId.newId();
        KPI initialKPI = new KPI(id, new Name("Old Name"), TargetDestination.DECREASING);
        adapter.save(initialKPI);

        KPI updatedKPI = new KPI(id, new Name("New Name"), TargetDestination.INCREASING);
        adapter.update(updatedKPI);

        Optional<KPI> result = adapter.findById(id);
        assertTrue(result.isPresent());

        KPI loaded = result.get();
        assertEquals(id, loaded.getId());
        assertEquals("New Name", loaded.getName().value());
        assertEquals(TargetDestination.INCREASING, loaded.getDestination());
    }

    @Test
    void update_nonExistentKPI_throwsException() {
        KPI kpi = new KPI(KPIId.newId(), new Name("Ghost"), TargetDestination.DECREASING);

        assertThrows(InfrastructureException.class, () -> adapter.update(kpi));
    }

    @Test
    void update_nullAsKPI_throwsException() {
        assertThrows(InfrastructureException.class, () -> adapter.update(null));
    }

    @Test
    void delete_idExists_removesKPI() {
        KPIId id = KPIId.newId();
        adapter.save(new KPI(id, new Name("To be deleted"), TargetDestination.DECREASING));

        assertTrue(adapter.findById(id).isPresent());

        adapter.delete(id);

        Optional<KPI> result = adapter.findById(id);
        assertTrue(result.isEmpty(), "KPI should be gone after deletion");

        Page<KPI> all = adapter.findByFilter(null, pageRequest);
        assertEquals(0, all.totalElements());
    }

    @Test
    void delete_idMissing_doesNothing() {
        KPIId existingId = KPIId.newId();
        adapter.save(new KPI(existingId, new Name("Some KPI"), TargetDestination.DECREASING));

        KPIId nonExistentId = KPIId.newId();
        adapter.delete(nonExistentId);

        assertTrue(adapter.findById(existingId).isPresent());

        Page<KPI> all = adapter.findByFilter(null, new PageRequest(1, 10));
        assertEquals(1, all.totalElements());
    }
}