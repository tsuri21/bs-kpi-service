package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.TargetDestinationDTO;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;
import jakarta.inject.Singleton;

@Singleton
public class TargetDestinationApiMapper implements ApiMapper<TargetDestination, TargetDestinationDTO> {

    @Override
    public TargetDestinationDTO toApiModel(TargetDestination domain) {
        return switch (domain) {
            case DECREASING -> TargetDestinationDTO.DECREASING;
            case RANGE -> TargetDestinationDTO.RANGE;
            case INCREASING -> TargetDestinationDTO.INCREASING;
        };
    }

    @Override
    public TargetDestination toDomainModel(TargetDestinationDTO dto) {
        return switch (dto) {
            case DECREASING -> TargetDestination.DECREASING;
            case RANGE -> TargetDestination.RANGE;
            case INCREASING -> TargetDestination.INCREASING;
        };
    }
}
