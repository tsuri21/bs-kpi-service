package de.thws.fiw.bs.kpi.application.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class KPIEntry {

    private UUID id;
    private LocalDateTime timestamp;
    private double value;

    public KPIEntry() {
    }

    public KPIEntry(UUID id, LocalDateTime timestamp, double value) {
        this.id = id;
        this.timestamp = validateTimestamp(timestamp);
        this.value = value;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = validateTimestamp(timestamp);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    private static LocalDateTime validateTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp must not be null");
        }
        if (timestamp.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Timestamp must not be in the future");
        }
        return timestamp;
    }
}
