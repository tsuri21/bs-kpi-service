package de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum StatusDTO {
    GREEN,
    YELLOW,
    RED;

    @JsonCreator
    public static StatusDTO fromString(String value) {
        return Arrays.stream(StatusDTO.values())
                .filter(v -> v.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown value for status: " + value));
    }
}
