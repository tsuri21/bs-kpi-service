package de.thws.fiw.bs.kpi.application.domain.model.user;

import java.util.Objects;

public record Username(String value) {
    public Username {
        Objects.requireNonNull(value, "Name must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (value.contains(":")) {
            throw new IllegalArgumentException("Name must not contain columns");
        }
    }
}
