package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.KPIDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.TargetDestinationDTO;
import de.thws.fiw.bs.kpi.application.domain.model.*;
import jakarta.inject.Singleton;

@Singleton
public class KPIApiMapper implements ApiMapper<KPI, KPIDTO> {

    @Override
    public KPIDTO toApiModel(KPI kpi) {
        return new KPIDTO(
                kpi.getId().value(),
                kpi.getName().value(),
                TargetDestinationDTO.valueOf(kpi.getDestination().name())
        );
    }

    @Override
    public KPI toDomainModel(KPIDTO kpiDto) {
        return new KPI(
                kpiDto.getId() == null ? KPIId.newId() : new KPIId(kpiDto.getId()),
                new Name(kpiDto.getName()),
                TargetDestination.valueOf(kpiDto.getDestination().name())
        );
    }
}
