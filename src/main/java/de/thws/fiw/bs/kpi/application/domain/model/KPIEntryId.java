package de.thws.fiw.bs.kpi.application.domain.model;

import java.util.Objects;
import java.util.UUID;

public record KPIEntryId(UUID value) {
    public KPIEntryId {
        Objects.requireNonNull(value, "KPIEntry id must not be null");
    }

    public static KPIEntryId newId() {
        return new KPIEntryId(UUID.randomUUID());
    }
}
