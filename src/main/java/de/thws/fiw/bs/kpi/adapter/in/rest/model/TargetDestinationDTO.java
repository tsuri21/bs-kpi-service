package de.thws.fiw.bs.kpi.adapter.in.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum TargetDestinationDTO {
    DECREASING, RANGE, INCREASING;

    @JsonCreator
    public static TargetDestinationDTO fromString(String value) {
        return Arrays.stream(TargetDestinationDTO.values())
                .filter(v -> v.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown value for target destination: " + value));
    }
}