package de.thws.fiw.bs.kpi.application.domain.model;

import java.util.Objects;
import java.util.UUID;

public record KPIId(UUID value) {
    public KPIId {
        Objects.requireNonNull(value, "KPI id must not be null");
    }

    public static KPIId newId() {
        return new KPIId(UUID.randomUUID());
    }
}
