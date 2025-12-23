package de.thws.fiw.bs.kpi.application.domain.model;

import java.util.Objects;

public record Name(String value) {
    public Name {
        Objects.requireNonNull(value, "Name must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
    }
}
