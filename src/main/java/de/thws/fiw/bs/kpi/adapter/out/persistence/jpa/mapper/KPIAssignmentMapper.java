package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.KPIAssignmentEntity;
import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.KPIEntity;
import de.thws.fiw.bs.kpi.application.domain.model.*;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class KPIAssignmentMapper {

//    @Inject
//    EntityManager em;

    public KPIAssignmentEntity toPersistenceModel(KPIAssignment kpiAssignment)
    {
        if (kpiAssignment == null) return null;
        return new KPIAssignmentEntity(
                kpiAssignment.getId().value(),
                kpiAssignment.getThresholds().getGreen(),
                kpiAssignment.getThresholds().getYellow(),
                kpiAssignment.getThresholds().getRed(),
                kpiAssignment.getKpiId().value(),
                kpiAssignment.getProjectId().value()
        );
    }

    public List<KPIAssignment> toDomainModels (List<KPIAssignmentEntity> kpiAssignmentsentities){
        return kpiAssignmentsentities.stream().map(this::toDomainModel).collect(Collectors.toList());
    }

    public KPIAssignment toDomainModel(KPIAssignmentEntity kpiAssignmentEntity){
        if (kpiAssignmentEntity == null) return null;

        //KPIEntity entity = em.getReference(KPIEntity.class, kpiAssignmentEntity.getKpiEntity().getId());
        return new KPIAssignment(
                new KPIAssignmentId(kpiAssignmentEntity.getId()),
                Thresholds.forDestination(kpiAssignmentEntity.getKpiEntity().getDestination(), kpiAssignmentEntity.getGreen(), kpiAssignmentEntity.getYellow(), kpiAssignmentEntity.getRed()),
                new KPIId(kpiAssignmentEntity.getKpiEntity().getId()),
                new ProjectId(kpiAssignmentEntity.getProjectId())
        );
    }
}
