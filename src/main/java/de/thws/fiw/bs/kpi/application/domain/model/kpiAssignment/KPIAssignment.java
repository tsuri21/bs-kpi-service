package de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment;

import de.thws.fiw.bs.kpi.application.domain.model.Thresholds;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;

import java.util.Objects;

public class KPIAssignment {
    private final KPIAssignmentId id;
    private final Thresholds thresholds;
    private final KPI kpi;
    private final ProjectId projectId;

    public KPIAssignment(KPIAssignmentId id, Thresholds thresholds, KPI kpi, ProjectId projectId) {
        this.id = Objects.requireNonNull(id, "KPIAssignment id must not be null");
        this.thresholds = Objects.requireNonNull(thresholds, "Thresholds must not be null");
        this.kpi = Objects.requireNonNull(kpi, "KPI must not be null");
        this.projectId = Objects.requireNonNull(projectId, "Project id must not be null");
    }

    public KPIAssignmentId getId() {
        return id;
    }

    public Thresholds getThresholds() {
        return thresholds;
    }

    public KPI getKpi() {
        return kpi;
    }

    public ProjectId getProjectId() {
        return projectId;
    }
}