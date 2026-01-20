package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiEntry.CreateKPIEntryDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiEntry.KPIEntryDTO;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntryId;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Singleton
public class KPIEntryApiMapper implements ApiMapper<KPIEntry, KPIEntryDTO> {

    @Inject
    Clock clock;

    @Override
    public KPIEntryDTO toApiModel(KPIEntry entry) {
        return new KPIEntryDTO(
                entry.getId().value(),
                entry.getKpiAssignmentId().value(),
                entry.getTimestamp(),
                entry.getValue()
        );
    }

    @Override
    public KPIEntry toDomainModel(KPIEntryDTO dto) {
        return KPIEntry.createNew(
                new KPIEntryId(dto.getId()),
                new KPIAssignmentId(dto.getKpiAssignmentId()),
                dto.getTimestamp(),
                dto.getMeasurement(),
                clock
        );
    }

    public KPIEntry toDomainModelByCreate(CreateKPIEntryDTO dto, UUID aId) {
        return KPIEntry.createNew(
                KPIEntryId.newId(),
                new KPIAssignmentId(aId),
                Instant.now(),
                dto.getMeasurement(),
                clock
        );
    }
}
