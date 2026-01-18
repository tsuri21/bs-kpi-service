package de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiEntry;

import jakarta.validation.constraints.NotNull;

public class CreateKPIEntryDTO {

    @NotNull(message = "Measurement must not be null")
    private double measurement;

    public CreateKPIEntryDTO() {
    }

    public CreateKPIEntryDTO(double measurement) {
        this.measurement = measurement;
    }

    public double getMeasurement() {
        return measurement;
    }

    public void setMeasurement(double measurement) {
        this.measurement = measurement;
    }

}
