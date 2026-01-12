package de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.AbstractDTO;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class UpdateKPIDTO extends AbstractDTO {

    @NotBlank(message = "Name must not be blank")
    private String name;

    public UpdateKPIDTO() {
    }

    public UpdateKPIDTO(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
