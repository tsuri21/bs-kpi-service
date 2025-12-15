package de.thws.fiw.bs.kpi.application.domain.model;

import java.util.UUID;

public class KPI {
    private UUID id;
    private String name;
    private TargetDestination destination;

    public KPI() {
    }

    public KPI(UUID id, String name, TargetDestination destination) {
        this.id = id;
        this.name = validateName(name);
        this.destination = validateDestination(destination);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = validateName(name);
    }

    public TargetDestination getDestination() {
        return destination;
    }

    public void setDestination(TargetDestination destination) {
        this.destination = validateDestination(destination);
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        return name;
    }

    private static TargetDestination validateDestination(TargetDestination destination) {
        if (destination == null) {
            throw new IllegalArgumentException("Destination must not be null");
        }
        return destination;
    }
}
