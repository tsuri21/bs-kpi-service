package de.thws.fiw.bs.kpi.application.domain.model;

import java.util.Objects;
import java.util.UUID;

public record KPIAssignmentId(UUID id) {
    public KPIAssignmentId {
        Objects.requireNonNull(id, "KPIAssignment id must not be null");
    }
}
