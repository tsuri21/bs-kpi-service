package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.KPIEvaluationResultDTO;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIEvaluationResult;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class KPIEvaluationResultApiMapper implements ToApiMapper<KPIEvaluationResult, KPIEvaluationResultDTO> {

    @Inject
    KPIApiMapper kpiApiMapper;

    @Inject
    KPIEntryApiMapper kpiEntryAPIMapper;

    @Inject
    StatusApiMapper statusApiMapper;

    @Override
    public KPIEvaluationResultDTO toApiModel(KPIEvaluationResult dto) {
        return new KPIEvaluationResultDTO(
                kpiApiMapper.toApiModel(dto.getKpi()),
                statusApiMapper.toApiModel(dto.getStatus()),
                kpiEntryAPIMapper.toApiModel(dto.getEntry())
        );
    }
}
