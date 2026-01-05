package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;

import java.util.Optional;

public interface KPIUseCase {
    Optional<KPI> readById(KPIId id);

    Page<KPI> readAll(Name name, PageRequest pageRequest);

    void create(KPI kpi);

    void update(KPI kpi);

    void delete(KPIId id);
}
