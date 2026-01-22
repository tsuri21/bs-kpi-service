package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"assignment_id", "timestamp"})
})
public class KPIEntryEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private KPIAssignmentEntity assignment;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private double measurement;

    public KPIEntryEntity() {
    }

    public KPIEntryEntity(UUID id, KPIAssignmentEntity assignment, Instant timestamp, double value) {
        this.id = id;
        this.assignment = assignment;
        this.timestamp = timestamp;
        this.measurement = value;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public KPIAssignmentEntity getAssignment() {
        return assignment;
    }

    public void setAssignment(KPIAssignmentEntity assignment) {
        this.assignment = assignment;
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
}
