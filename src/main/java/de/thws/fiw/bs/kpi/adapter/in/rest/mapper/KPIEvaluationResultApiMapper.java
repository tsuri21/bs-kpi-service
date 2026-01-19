package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.KPIEvaluationResultDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.StatusDTO;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIEvaluationResult;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class KPIEvaluationResultApiMapper {
    @Inject
    KPIApiMapper kpiApiMapper;

    @Inject
    KPIEntryAPIMapper kpiEntryAPIMapper;

    public KPIEvaluationResultDTO toApiModel(KPIEvaluationResult obj) {
        return new KPIEvaluationResultDTO(
                kpiApiMapper.toApiModel(obj.getKpi()),
                StatusDTO.valueOf(obj.getStatus().name()),
                kpiEntryAPIMapper.toApiModel(obj.getEntry())
        );
    }

    public List<KPIEvaluationResultDTO> toApiModelList(List<KPIEvaluationResult> objList) {
        if (objList == null) {
            return List.of();
        }
        return objList.stream()
                .map(this::toApiModel)
                .toList();
    }
}
