package de.thws.fiw.bs.kpi.application.port.out;

import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;

import java.util.Optional;

public interface KPIRepository {
    Optional<KPI> findById(KPIId id);

    Page<KPI> findByFilter(Name name, PageRequest pageRequest);

    void save(KPI kpi);

    void update(KPI kpi);

    void delete(KPIId id);
}
