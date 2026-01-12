package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.CreateKPIDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.KPIDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.TargetDestinationDTO;
import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;
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
                new KPIId(kpiDto.getId()),
                new Name(kpiDto.getName()),
                TargetDestination.valueOf(kpiDto.getDestination().name())
        );
    }

    public KPI toDomainModelByCreate(CreateKPIDTO createKPIDto) {
        return new KPI(
                KPIId.newId(),
                new Name(createKPIDto.getName()),
                TargetDestination.valueOf(createKPIDto.getDestination().name())
        );
    }
}
