package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.out.KPIRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class KPIServiceTest {

    @Inject
    KPIService kpiService;

    @InjectMock
    KPIRepository kpiRepository;

    @Test
    void readById_idGiven_callsRepository() {
        KPIId id = KPIId.newId();

        kpiService.readById(id);

        verify(kpiRepository).findById(id);
    }

    @Test
    void readAll_filtersGiven_callsRepository() {
        Name name = new Name("Test");
        PageRequest pageRequest = new PageRequest(1, 10);

        kpiService.readAll(name, pageRequest);

        verify(kpiRepository).findByFilter(name, pageRequest);
    }

    @Test
    void create_kpiGiven_callsRepository() {
        KPI kpi = mock(KPI.class);

        kpiService.create(kpi);

        verify(kpiRepository).save(kpi);
    }

    @Test
    void updateName_kpiExists_setsNameAndCallsRepositoryUpdate() {
        KPIId id = KPIId.newId();
        Name newName = new Name("New");

        KPI existing = new KPI(id, new Name("Old"), TargetDestination.DECREASING);
        when(kpiRepository.findById(id)).thenReturn(Optional.of(existing));

        kpiService.updateName(id, newName);
        ArgumentCaptor<KPI> captor = ArgumentCaptor.forClass(KPI.class);
        verify(kpiRepository).update(captor.capture());

        KPI updated = captor.getValue();
        assertEquals(newName, updated.getName());
    }

    @Test
    void update_kpiDoesNotExist_throwsException() {
        KPIId id = KPIId.newId();
        KPI kpiToUpdate = mock(KPI.class);

        when(kpiToUpdate.getId()).thenReturn(id);
        when(kpiRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiService.update(kpiToUpdate));
        verify(kpiRepository, never()).update(any());
    }

    @Test
    void delete_kpiExists_callsRepository() {
        KPIId id = KPIId.newId();
        KPI kpi = mock(KPI.class);

        when(kpiRepository.findById(id)).thenReturn(Optional.of(kpi));

        kpiService.delete(id);
        verify(kpiRepository).delete(id);
    }

    @Test
    void delete_kpiDoesNotExist_throwsException() {
        KPIId id = KPIId.newId();

        when(kpiRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiService.delete(id));
        verify(kpiRepository, never()).delete(any());
    }
}