package de.thws.fiw.bs.kpi.adapter.in.rest.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class KPIDTO extends AbstractDTO {
    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotNull(message = "Target destination is required")
    private TargetDestinationDTO destination;

    public KPIDTO() {
    }

    public KPIDTO(UUID id, String name, TargetDestinationDTO destination) {
        this.id = id;
        this.name = name;
        this.destination = destination;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TargetDestinationDTO getDestination() {
        return destination;
    }

    public void setDestination(TargetDestinationDTO destination) {
        this.destination = destination;
    }
}
