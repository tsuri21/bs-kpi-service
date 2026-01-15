package de.thws.fiw.bs.kpi.application.domain.model.kpiEntry;

import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class KPIEntry {

    private final KPIEntryId id;
    private final KPIAssignmentId kpiAssignmentId;
    private final Instant timestamp;
    private final double value;

    private KPIEntry(KPIEntryId id, KPIAssignmentId kpiAssignmentId, Instant timestamp, double value) {
        this.id = Objects.requireNonNull(id, "KPIEntry id must not be null");
        this.kpiAssignmentId = Objects.requireNonNull(kpiAssignmentId, "KPIAssignment id must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp must not be null");
        this.value = value;
    }

    public static KPIEntry createNew(KPIEntryId id, KPIAssignmentId kpiAssignmentId, Instant timestamp, double value, Clock clock) {
        Objects.requireNonNull(clock, "Clock must not be null");
        if (timestamp.isAfter(Instant.now(clock))) {
            throw new IllegalArgumentException("Timestamp must not be in the future");
        }
        return new KPIEntry(id, kpiAssignmentId, timestamp, value);
    }

    public static KPIEntry reconstruct(KPIEntryId id, KPIAssignmentId kpiAssignmentId, Instant timestamp, double value) {
        return new KPIEntry(id, kpiAssignmentId, timestamp, value);
    }

    public KPIEntryId getId() {
        return id;
    }

    public KPIAssignmentId getKpiAssignmentId() {
        return kpiAssignmentId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }
}
