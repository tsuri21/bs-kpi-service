package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.KPIAssignmentEntity;
import de.thws.fiw.bs.kpi.application.domain.model.*;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class KPIAssignmentJpaMapper implements PersistenceMapper<KPIAssignment, KPIAssignmentEntity> {

    @Inject
    KPIJpaMapper mapper;

    @Override
    public KPIAssignmentEntity toPersistenceModel(KPIAssignment kpiAssignment) {
        return new KPIAssignmentEntity(
                kpiAssignment.getId().value(),
                kpiAssignment.getThresholds().getGreen(),
                kpiAssignment.getThresholds().getYellow(),
                kpiAssignment.getThresholds().getRed(),
                mapper.toPersistenceModel(kpiAssignment.getKpi()),
                kpiAssignment.getProjectId().value()
        );
    }

    @Override
    public KPIAssignment toDomainModel(KPIAssignmentEntity kpiAssignmentEntity) {
        return new KPIAssignment(
                new KPIAssignmentId(kpiAssignmentEntity.getId()),
                Thresholds.forDestination(kpiAssignmentEntity.getKpiEntity().getDestination(), kpiAssignmentEntity.getGreen(), kpiAssignmentEntity.getYellow(), kpiAssignmentEntity.getRed()),
                mapper.toDomainModel(kpiAssignmentEntity.getKpiEntity()),
                new ProjectId(kpiAssignmentEntity.getProjectId())
        );
    }
}
