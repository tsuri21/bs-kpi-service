package de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiEntry.KPIEntryDTO;

public record KPIEvaluationResultDTO(
        KPIDTO kpi,
        StatusDTO status,
        KPIEntryDTO kpiEntry) {
}
