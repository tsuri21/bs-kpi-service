package de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateKPIDTO {

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotNull(message = "Target destination is required")
    private TargetDestinationDTO destination;

    public CreateKPIDTO() {
    }

    public CreateKPIDTO(String name, TargetDestinationDTO destination) {
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
