package de.thws.fiw.bs.kpi.application.domain.model;

import java.util.Objects;

public class KPI {
    private final KPIId id;
    private final Name name;
    private final TargetDestination destination;

    public KPI(KPIId id, Name name, TargetDestination destination) {
        this.id = Objects.requireNonNull(id, "Id must not be null");
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.destination = Objects.requireNonNull(destination, "Target destination must not be null");
    }

    public KPIId getId() {
        return id;
    }

    public Name getName() {
        return name;
    }

    public TargetDestination getDestination() {
        return destination;
    }
}
