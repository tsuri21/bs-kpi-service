package de.thws.fiw.bs.kpi.application.port.out;

import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntryId;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;

import java.time.Instant;
import java.util.Optional;

public interface KPIEntryRepository {
    Optional<KPIEntry> findById(KPIEntryId id);

    Optional<KPIEntry> findLatest(KPIAssignmentId id);

    Page<KPIEntry> findByFilter(KPIAssignmentId id, Instant from, Instant to, PageRequest pageRequest);

    void save(KPIEntry kpiEntry);

    void update(KPIEntry kpiEntry);

    void delete(KPIEntryId id);
}
