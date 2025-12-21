package de.thws.fiw.bs.kpi.application.domain.model;

import java.util.Objects;

public class KPIAssignment {
    private final KPIAssignmentId id;
    private final Thresholds thresholds;
    private final KPIId kpiId;
    private final ProjectId projectId;

    public KPIAssignment(KPIAssignmentId id, Thresholds thresholds, KPIId kpiId, ProjectId projectId) {
        this.id = Objects.requireNonNull(id, "KPIAssignment id must not be null");
        this.thresholds = Objects.requireNonNull(thresholds, "Thresholds must not be null");
        this.kpiId = Objects.requireNonNull(kpiId, "KPI id must not be null");
        this.projectId = Objects.requireNonNull(projectId, "Project id must not be null");
    }

    public KPIAssignmentId getId() {
        return id;
    }

    public Thresholds getThresholds() {
        return thresholds;
    }

    public KPIId getKpiId() {
        return kpiId;
    }

    public ProjectId getProjectId() {
        return projectId;
    }
}