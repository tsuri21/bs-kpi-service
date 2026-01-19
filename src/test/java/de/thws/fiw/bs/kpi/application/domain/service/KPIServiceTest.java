package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;
import de.thws.fiw.bs.kpi.application.port.out.KPIRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KPIServiceTest {

    @InjectMocks
    KPIService kpiService;

    @Mock
    KPIRepository kpiRepository;

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
    void updateName_kpiDoesNotExist_throwsException() {
        KPIId id = KPIId.newId();
        Name newName = new Name("Test");

        when(kpiRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiService.updateName(id, newName));

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