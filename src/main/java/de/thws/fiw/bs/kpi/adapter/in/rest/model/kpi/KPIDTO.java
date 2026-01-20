package de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.AbstractDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        KPIDTO kpidto = (KPIDTO) o;
        return Objects.equals(name, kpidto.name) && destination == kpidto.destination;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, destination);
    }
}
