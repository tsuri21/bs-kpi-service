package de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiAssignment;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateKPIAssignmentDTO {
    @NotNull()
    private double green;

    @NotNull
    private double yellow;

    private Double targetValue;

    @NotNull()
    private UUID kpiId;

    public CreateKPIAssignmentDTO() {
    }

    public CreateKPIAssignmentDTO(double green, double yellow, Double targetValue, UUID kpiId) {
        this.green = green;
        this.yellow = yellow;
        this.targetValue = targetValue;
        this.kpiId = kpiId;
    }

    public UUID getKpiId() {
        return kpiId;
    }

    public void setKpi(UUID kpiId) {
        this.kpiId = kpiId;
    }

    public double getGreen() {
        return green;
    }

    public void setGreen(double green) {
        this.green = green;
    }

    public double getYellow() {
        return yellow;
    }

    public void setYellow(double yellow) {
        this.yellow = yellow;
    }

    public Double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(Double targetValue) {
        this.targetValue = targetValue;
    }

    public void setKpiId(UUID kpiId) {
        this.kpiId = kpiId;
    }
}
