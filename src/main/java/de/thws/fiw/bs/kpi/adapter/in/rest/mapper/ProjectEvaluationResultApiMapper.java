package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.StatusDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.project.ProjectEvaluationResultDTO;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectEvaluationResult;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ProjectEvaluationResultApiMapper {

    @Inject
    ProjectApiMapper projectApiMapper;

    @Inject
    KPIEvaluationResultApiMapper evaluationMapper;

    public ProjectEvaluationResultDTO toApiModel(ProjectEvaluationResult obj) {
        return new ProjectEvaluationResultDTO(
                projectApiMapper.toApiModel(obj.getProject()),
                StatusDTO.valueOf(obj.getStatus().name()),
                evaluationMapper.toApiModel(obj.getFocusKpi()),
                evaluationMapper.toApiModelList(obj.getAllKpis())
        );
    }
}
