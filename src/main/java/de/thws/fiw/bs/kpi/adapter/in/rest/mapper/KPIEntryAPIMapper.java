package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiEntry.CreateKPIEntryDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiEntry.KPIEntryDTO;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntryId;
import jakarta.inject.Singleton;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

@Singleton
public class KPIEntryAPIMapper implements ApiMapper<KPIEntry, KPIEntryDTO> {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2027-01-01T10:00:00Z"), ZoneOffset.UTC);

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
                FIXED_CLOCK
        );
    }

    public KPIEntry toDomainModelByCreate(CreateKPIEntryDTO dto, UUID aId) {
        return KPIEntry.createNew(
                KPIEntryId.newId(),
                new KPIAssignmentId(aId),
                Instant.now(),
                dto.getMeasurement(),
                FIXED_CLOCK
        );
    }
}
