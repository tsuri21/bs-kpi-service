package de.thws.fiw.bs.kpi.application.domain.model.user;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {
    public UserId {
        Objects.requireNonNull(value, "User id must not be null");
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }
}
