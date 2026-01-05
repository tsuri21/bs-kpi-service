package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"kpi_id", "project_id"})
})
public class KPIAssignmentEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private double green;

    @Column(nullable = false)
    private double yellow;

    @Column(nullable = false)
    private double red;

    @ManyToOne
    @JoinColumn(name = "kpi_id", nullable = false)
    private KPIEntity kpiEntity;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;


    public KPIAssignmentEntity() {
    }

    public KPIAssignmentEntity(UUID id, double green, double yellow, double red, KPIEntity kpi, UUID projectId) {
        this.id = id;
        this.green = green;
        this.yellow = yellow;
        this.red = red;
        this.kpiEntity = kpi;
        this.projectId = projectId;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public double getGreen() {
        return green;
    }

    public void setGreen(double green) {
        this.green = green;
    }

    public double getYellow() {
        return yellow;
    }

    public void setYellow(double yellow) {
        this.yellow = yellow;
    }

    public double getRed() {
        return red;
    }

    public void setRed(double red) {
        this.red = red;
    }


    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }


    public KPIEntity getKpiEntity() {
        return kpiEntity;
    }

    public void setKpiEntity(KPIEntity kpiEntity) {
        this.kpiEntity = kpiEntity;
    }

}
