package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiAssignment.CreateKPIAssignmentDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiAssignment.KPIAssignmentDTO;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.port.in.KPIAssignmentCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.UUID;

@Singleton
public class KPIAssignmentAPIMapper {

    @Inject
    KPIApiMapper mapper;

    public KPIAssignmentDTO toApiModel(KPIAssignment kpiAssignment) {
        return new KPIAssignmentDTO(
                kpiAssignment.getId().value(),
                kpiAssignment.getThresholds().getGreen(),
                kpiAssignment.getThresholds().getYellow(),
                kpiAssignment.getThresholds().getTargetValue(),
                mapper.toApiModel(kpiAssignment.getKpi()).getId(),
                kpiAssignment.getProjectId().value()
        );
    }

    public KPIAssignmentCommand toDomainModel(KPIAssignmentDTO dto) {
        return new KPIAssignmentCommand(
                new KPIAssignmentId(dto.getId()),
                new KPIId(dto.getKpiId()),
                new ProjectId(dto.getProjectId()),
                dto.getGreen(),
                dto.getYellow(),
                dto.getTargetValue()
        );
    }

    public KPIAssignmentCommand toDomainModelByCreate(CreateKPIAssignmentDTO dto, KPIAssignmentId kpiAssignmentId, UUID projectId) {
        return new KPIAssignmentCommand(
                kpiAssignmentId,
                new KPIId(dto.getKpiId()),
                new ProjectId(projectId),
                dto.getGreen(),
                dto.getYellow(),
                dto.getTargetValue()
        );
    }

    public List<KPIAssignmentDTO> toApiModels(List<KPIAssignment> domains) {
        return domains == null ? List.of() : domains.stream()
                .map(this::toApiModel)
                .toList();
    }

    public List<KPIAssignmentCommand> toDomainModels(List<KPIAssignmentDTO> dtos) {
        return dtos == null ? List.of() : dtos.stream()
                .map(this::toDomainModel)
                .toList();
    }
}
