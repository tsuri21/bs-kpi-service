package de.thws.fiw.bs.kpi.application.domain.model.user;

import java.util.Objects;

public record Username(String value) {
    public Username {
        Objects.requireNonNull(value, "Username must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        if (value.contains(":")) {
            throw new IllegalArgumentException("Username must not contain colons");
        }
    }
}
