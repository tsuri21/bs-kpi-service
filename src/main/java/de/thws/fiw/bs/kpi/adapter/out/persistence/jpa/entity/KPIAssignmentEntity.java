package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
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

    @Column
    private Double targetValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kpi_id", nullable = false)
    private KPIEntity kpi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<KPIEntryEntity> entries = new ArrayList<>();

    public KPIAssignmentEntity() {
    }

    public KPIAssignmentEntity(UUID id, double green, double yellow, Double targetValue, KPIEntity kpi, ProjectEntity project) {
        this.id = id;
        this.green = green;
        this.yellow = yellow;
        this.targetValue = targetValue;
        this.kpi = kpi;
        this.project = project;
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

    public Double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(Double targetValue) {
        this.targetValue = targetValue;
    }

    public KPIEntity getKpi() {
        return kpi;
    }

    public void setKpi(KPIEntity kpiEntity) {
        this.kpi = kpiEntity;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }
}
