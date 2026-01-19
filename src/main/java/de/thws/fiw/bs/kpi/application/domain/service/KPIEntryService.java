package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntryId;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.KPIEntryUseCase;
import de.thws.fiw.bs.kpi.application.port.out.KPIAssignmentRepository;
import de.thws.fiw.bs.kpi.application.port.out.KPIEntryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class KPIEntryService implements KPIEntryUseCase {

    @Inject
    KPIEntryRepository kpiEntryRepository;

    @Inject
    KPIAssignmentRepository kpiAssignmentRepository;

    @Override
    public Optional<KPIEntry> readById(KPIEntryId id) {
        return kpiEntryRepository.findById(id);
    }

    @Override
    public Page<KPIEntry> readAll(KPIAssignmentId id, Instant from, Instant to, PageRequest pageRequest) {
        return kpiEntryRepository.findByFilter(id, from, to, pageRequest);
    }

    @Override
    public void create(KPIEntry kpiEntry) {
        kpiAssignmentRepository.findById(kpiEntry.getKpiAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("KPIAssignment", kpiEntry.getKpiAssignmentId()));

        kpiEntryRepository.save(kpiEntry);
    }

    @Override
    public void delete(KPIEntryId id) {
        kpiEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPIEntry", id));

        kpiEntryRepository.delete(id);
    }
}
