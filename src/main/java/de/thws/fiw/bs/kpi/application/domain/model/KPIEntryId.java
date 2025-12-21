package de.thws.fiw.bs.kpi.application.domain.model;

import java.util.Objects;
import java.util.UUID;

public record KPIEntryId(UUID id) {
    public KPIEntryId {
        Objects.requireNonNull(id, "KPIEntry id must not be null");
    }
}
