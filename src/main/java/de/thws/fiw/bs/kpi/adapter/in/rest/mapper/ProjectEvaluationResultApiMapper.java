package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.project.ProjectEvaluationResultDTO;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectEvaluationResult;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ProjectEvaluationResultApiMapper implements ToApiMapper<ProjectEvaluationResult, ProjectEvaluationResultDTO> {

    @Inject
    ProjectApiMapper projectApiMapper;

    @Inject
    KPIEvaluationResultApiMapper kpiEvaluationMapper;

    @Inject
    StatusApiMapper statusApiMapper;

    @Override
    public ProjectEvaluationResultDTO toApiModel(ProjectEvaluationResult dto) {
        return new ProjectEvaluationResultDTO(
                projectApiMapper.toApiModel(dto.getProject()),
                statusApiMapper.toApiModel(dto.getStatus()),
                kpiEvaluationMapper.toApiModel(dto.getFocusKpi()),
                kpiEvaluationMapper.toApiModels(dto.getAllKpis())
        );
    }
}
