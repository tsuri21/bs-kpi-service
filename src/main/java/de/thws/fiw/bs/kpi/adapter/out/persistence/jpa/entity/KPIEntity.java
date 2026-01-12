package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity;

import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class KPIEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private TargetDestination destination;

    public KPIEntity() {
    }

    public KPIEntity(UUID id, String name, TargetDestination destination) {
        this.id = id;
        this.name = name;
        this.destination = destination;
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
        this.name = name;
    }

    public TargetDestination getDestination() {
        return destination;
    }

    public void setDestination(TargetDestination destination) {
        this.destination = destination;
    }
}
