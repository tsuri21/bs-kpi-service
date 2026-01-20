package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.StatusDTO;
import de.thws.fiw.bs.kpi.application.domain.model.Status;
import jakarta.inject.Singleton;

@Singleton
public class StatusApiMapper implements ToApiMapper<Status, StatusDTO> {

    @Override
    public StatusDTO toApiModel(Status domain) {
        return switch (domain) {
            case GREEN -> StatusDTO.GREEN;
            case YELLOW -> StatusDTO.YELLOW;
            case RED -> StatusDTO.RED;
        };
    }
}
