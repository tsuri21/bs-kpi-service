package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;

public interface KPIUseCase {
    KPI readById(ProjectId id);

    Page<KPI> readAll(Name name, PageRequest pageRequest);

    void create(KPI kpi);

    void update(KPIId id, KPI kpi);

    void delete(KPIId id);
}
