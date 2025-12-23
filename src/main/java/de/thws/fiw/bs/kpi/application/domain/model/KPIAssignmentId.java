package de.thws.fiw.bs.kpi.application.domain.model;

import java.util.Objects;
import java.util.UUID;

public record KPIAssignmentId(UUID value) {
    public KPIAssignmentId {
        Objects.requireNonNull(value, "KPIAssignment id must not be null");
    }

    public static KPIAssignmentId newId() {
        return new KPIAssignmentId(UUID.randomUUID());
    }
}
