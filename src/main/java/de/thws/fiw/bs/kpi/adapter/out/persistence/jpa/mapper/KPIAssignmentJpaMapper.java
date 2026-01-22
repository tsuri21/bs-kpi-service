package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.KPIAssignmentEntity;
import de.thws.fiw.bs.kpi.application.domain.model.*;

import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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
                kpiAssignment.getThresholds().getTargetValue(),
                mapper.toPersistenceModel(kpiAssignment.getKpi()),
                null
        );
    }

    @Override
    public KPIAssignment toDomainModel(KPIAssignmentEntity kpiAssignmentEntity) {
        Thresholds thresholds;
        TargetDestination dest = kpiAssignmentEntity.getKpi().getDestination();

        if (dest == TargetDestination.RANGE) {
            thresholds = Thresholds.range(kpiAssignmentEntity.getTargetValue(), kpiAssignmentEntity.getGreen(), kpiAssignmentEntity.getYellow());
        } else {
            thresholds = Thresholds.linear(dest, kpiAssignmentEntity.getGreen(), kpiAssignmentEntity.getYellow());
        }

        return new KPIAssignment(
                new KPIAssignmentId(kpiAssignmentEntity.getId()),
                thresholds,
                mapper.toDomainModel(kpiAssignmentEntity.getKpi()),
                new ProjectId(kpiAssignmentEntity.getProject().getId())
        );
    }
}
