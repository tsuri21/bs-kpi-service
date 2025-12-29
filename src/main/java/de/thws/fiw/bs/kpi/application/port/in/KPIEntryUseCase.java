package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;

import java.time.Instant;

public interface KPIEntryUseCase {
    KPIEntry readById(KPIEntryId id);

    Page<KPIEntry> readAll(KPIAssignmentId id, Instant timestamp, PageRequest pageRequest);

    void create(KPIEntry kpiEntry);

    void update(KPIEntryId id, KPIEntry kpiEntry);

    void delete(KPIEntryId id);
}
