package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.KPIEntity;
import de.thws.fiw.bs.kpi.application.domain.model.*;
import jakarta.inject.Singleton;

@Singleton
public class KPIJpaMapper implements PersistenceMapper<KPI, KPIEntity> {

    @Override
    public KPIEntity toPersistenceModel(KPI kpi) {
        return new KPIEntity(
                kpi.getId().value(),
                kpi.getName().value(),
                kpi.getDestination()
        );
    }

    @Override
    public KPI toDomainModel(KPIEntity kpiEntity) {
        return new KPI(
                new KPIId(kpiEntity.getId()),
                new Name(kpiEntity.getName()),
                kpiEntity.getDestination()
        );
    }
}
