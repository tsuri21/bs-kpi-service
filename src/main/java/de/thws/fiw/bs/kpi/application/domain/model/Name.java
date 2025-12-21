package de.thws.fiw.bs.kpi.application.domain.model;

import java.util.Objects;

public record Name(String name) {
    public Name {
        Objects.requireNonNull(name, "Name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
    }
}
