package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;

import java.time.Instant;
import java.util.Optional;

public interface KPIEntryUseCase {
    Optional<KPIEntry> readById(KPIEntryId id);

    Page<KPIEntry> readAll(KPIAssignmentId id, Instant timestamp, PageRequest pageRequest);

    void create(KPIEntry kpiEntry);

    void update(KPIEntry kpiEntry);

    void delete(KPIEntryId id);
}
