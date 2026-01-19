package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntryId;
import de.thws.fiw.bs.kpi.application.port.out.KPIAssignmentRepository;
import de.thws.fiw.bs.kpi.application.port.out.KPIEntryRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class KPIEntryServiceTest {

    @Inject
    KPIEntryService kpiEntryService;

    @InjectMock
    KPIEntryRepository kpiEntryRepository;

    @InjectMock
    KPIAssignmentRepository kpiAssignmentRepository;

    @Test
    void create_assignmentExists_callsRepositorySave() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIEntry kpiEntry = mock(KPIEntry.class);

        when(kpiEntry.getKpiAssignmentId()).thenReturn(assignmentId);

        when(kpiAssignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(mock(KPIAssignment.class)));

        kpiEntryService.create(kpiEntry);

        verify(kpiEntryRepository).save(kpiEntry);
    }

    @Test
    void create_assignmentDoesNotExist_throwsException() {
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        KPIEntry kpiEntry = mock(KPIEntry.class);

        when(kpiEntry.getKpiAssignmentId()).thenReturn(assignmentId);

        when(kpiAssignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiEntryService.create(kpiEntry));

        verify(kpiEntryRepository, never()).save(any());
    }

    @Test
    void delete_kpiEntryExists_callsRepository() {
        KPIEntryId id = KPIEntryId.newId();
        KPIEntry kpiEntry = mock(KPIEntry.class);

        when(kpiEntryRepository.findById(id)).thenReturn(Optional.of(kpiEntry));

        kpiEntryService.delete(id);
        verify(kpiEntryRepository).delete(id);
    }

    @Test
    void delete_kpiEntryDoesNotExist_throwsException() {
        KPIEntryId id = KPIEntryId.newId();

        when(kpiEntryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kpiEntryService.delete(id));
        verify(kpiEntryRepository, never()).delete(any());
    }

}