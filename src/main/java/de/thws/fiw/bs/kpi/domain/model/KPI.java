package de.thws.fiw.bs.kpi.domain.model;

import java.util.UUID;

public class KPI {
    private UUID id;
    private String name;
    private TargetDestination destination;

    public KPI() {
    }

    public KPI(UUID id, String name, TargetDestination destination) {
        this.id = id;
        setName(name);
        this.destination = destination;
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        this.name = name;
    }

    public TargetDestination getDestination() {
        return destination;
    }

    public void setDestination(TargetDestination destination) {
        this.destination = destination;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
