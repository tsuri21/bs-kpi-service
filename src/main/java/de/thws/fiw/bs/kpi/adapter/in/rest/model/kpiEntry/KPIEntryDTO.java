package de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiEntry;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.AbstractDTO;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class KPIEntryDTO extends AbstractDTO {

    @NotNull(message = "KpiAssignmentId must not be null")
    private UUID kpiAssignmentId;

    @NotNull(message = "Timestamp must not be null")
    private Instant timestamp;

    @NotNull(message = "Measurement must not be null")
    private double measurement;


    public KPIEntryDTO() {
    }

    public KPIEntryDTO(UUID id, UUID kpiAssignmentId, Instant timestamp, double measurement) {
        this.id = id;
        this.kpiAssignmentId = kpiAssignmentId;
        this.timestamp = timestamp;
        this.measurement = measurement;
    }

    public UUID getKpiAssignmentId() {
        return kpiAssignmentId;
    }

    public void setKpiAssignmentId(UUID kpiAssignmentId) {
        this.kpiAssignmentId = kpiAssignmentId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public double getMeasurement() {
        return measurement;
    }

    public void setMeasurement(double measurement) {
        this.measurement = measurement;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        KPIEntryDTO that = (KPIEntryDTO) o;
        return Double.compare(measurement, that.measurement) == 0 && Objects.equals(kpiAssignmentId, that.kpiAssignmentId) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kpiAssignmentId, timestamp, measurement);
    }
}
