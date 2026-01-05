package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
import de.thws.fiw.bs.kpi.application.domain.model.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.KPIUseCase;
import de.thws.fiw.bs.kpi.application.port.out.KPIRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class KPIService implements KPIUseCase {

    @Inject
    KPIRepository kpiRepository;

    @Override
    public Optional<KPI> readById(KPIId id) {
        return kpiRepository.findById(id);
    }

    @Override
    public Page<KPI> readAll(Name name, PageRequest pageRequest) {
        return kpiRepository.findByFilter(name, pageRequest);
    }

    @Override
    public void create(KPI kpi) {
        kpiRepository.save(kpi);
    }

    @Override
    public void update(KPI kpi) {
        kpiRepository.findById(kpi.getId())
                .orElseThrow(() -> new ResourceNotFoundException("KPI", kpi.getId()));

        kpiRepository.update(kpi);
    }

    @Override
    public void delete(KPIId id) {
        kpiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPI", id));

        kpiRepository.delete(id);
    }
}
