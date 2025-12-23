package de.thws.fiw.bs.kpi.application.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ProjectId(UUID value) {
    public ProjectId {
        Objects.requireNonNull(value, "Project id must not be null");
    }

    public static ProjectId newId() {
        return new ProjectId(UUID.randomUUID());
    }
}

