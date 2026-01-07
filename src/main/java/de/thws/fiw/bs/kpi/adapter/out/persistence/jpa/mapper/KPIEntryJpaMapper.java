package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.KPIEntryEntity;
import de.thws.fiw.bs.kpi.application.domain.model.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.KPIEntryId;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Clock;


@Singleton
public class KPIEntryJpaMapper implements PersistenceMapper<KPIEntry, KPIEntryEntity> {

    @Inject
    Clock clock;

    @Override
    public KPIEntryEntity toPersistenceModel(KPIEntry kpiEntry) {
        return new KPIEntryEntity(
                kpiEntry.getId().value(),
                kpiEntry.getKpiAssignmentId().value(),
                kpiEntry.getTimestamp(),
                kpiEntry.getValue()
        );
    }

    @Override
    public KPIEntry toDomainModel(KPIEntryEntity kpiEntryEntity) {
        return new KPIEntry(
                new KPIEntryId(kpiEntryEntity.getId()),
                new KPIAssignmentId(kpiEntryEntity.getAssignmentId()),
                kpiEntryEntity.getTimestamp(),
                kpiEntryEntity.getMeasurement(),
                clock
        );
    }
}
