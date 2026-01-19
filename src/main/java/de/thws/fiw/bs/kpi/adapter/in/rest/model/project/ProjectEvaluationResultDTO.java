package de.thws.fiw.bs.kpi.adapter.in.rest.model.project;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.KPIEvaluationResultDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.StatusDTO;

import java.util.List;

public record ProjectEvaluationResultDTO(
        ProjectDTO project,
        StatusDTO status,
        KPIEvaluationResultDTO focusKpi,
        List<KPIEvaluationResultDTO> allKpis
) {
}
