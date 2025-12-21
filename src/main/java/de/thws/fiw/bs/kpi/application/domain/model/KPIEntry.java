package de.thws.fiw.bs.kpi.application.domain.model;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class KPIEntry {

    private final KPIEntryId id;
    private final KPIAssignmentId kpiAssignmentId;
    private final Instant timestamp;
    private final double value;

    public KPIEntry(KPIEntryId id, KPIAssignmentId kpiAssignmentId, Instant timestamp, double value, Clock clock) {
        this.id = Objects.requireNonNull(id, "KPIEntry id must not be null");
        this.kpiAssignmentId = Objects.requireNonNull(kpiAssignmentId, "KPIAssignment id must not be null");
        this.timestamp = validateTimestamp(timestamp, clock);
        this.value = value;
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

    private static Instant validateTimestamp(Instant ts, Clock clock) {
        Objects.requireNonNull(clock, "Clock must not be null");
        Objects.requireNonNull(ts, "Timestamp must not be null");

        if (ts.isAfter(Instant.now(clock))) {
            throw new IllegalArgumentException("Timestamp must not be in the future");
        }
        return ts;
    }
}
