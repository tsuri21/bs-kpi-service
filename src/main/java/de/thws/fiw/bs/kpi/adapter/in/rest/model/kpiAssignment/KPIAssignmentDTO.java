package de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiAssignment;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.AbstractDTO;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

public class KPIAssignmentDTO extends AbstractDTO {


    @NotNull()
    private double green;

    @NotNull
    private double yellow;

    private Double targetValue;

    @NotNull()
    private UUID kpiId;

    @NotNull()
    private UUID projectId;

    public KPIAssignmentDTO() {
    }

    public KPIAssignmentDTO(UUID id, double green, double yellow, Double targetValue, UUID kpiId, UUID projectId) {
        this.id = id;
        this.green = green;
        this.yellow = yellow;
        this.targetValue = targetValue;
        this.kpiId = kpiId;
        this.projectId = projectId;
    }

    public UUID getKpiId() {
        return kpiId;
    }

    public void setKpi(UUID kpiId) {
        this.kpiId = kpiId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
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

    @Override
    public boolean equals(Object o) {

        if (o == null || getClass() != o.getClass()) return false;
        KPIAssignmentDTO that = (KPIAssignmentDTO) o;
        return Double.compare(green, that.green) == 0 && Double.compare(yellow, that.yellow) == 0 && Objects.equals(targetValue, that.targetValue) && Objects.equals(kpiId, that.kpiId) && Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(green, yellow, targetValue, kpiId, projectId);
    }
}
