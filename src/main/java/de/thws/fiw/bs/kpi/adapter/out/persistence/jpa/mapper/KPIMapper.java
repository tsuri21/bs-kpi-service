package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.KPIEntity;
import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.ProjectEntity;
import de.thws.fiw.bs.kpi.application.domain.model.*;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class KPIMapper {
    public KPIEntity toPersistenceModel(KPI kpi) {
        if (kpi == null) return null;
        return new KPIEntity(
                kpi.getId().value(),
                kpi.getName().value(),
                kpi.getDestination()
        );
    }

    public List<KPI> toDomainModels(List<KPIEntity> kpiEntities) {
        return kpiEntities.stream().map(this::toDomainModel).collect(Collectors.toList());
    }

    public KPI toDomainModel(KPIEntity kpiEntity) {
        if (kpiEntity == null) return null;
        return new KPI(
                new KPIId(kpiEntity.getId()),
                new Name(kpiEntity.getName()),
                kpiEntity.getDestination()
        );
    }
}
