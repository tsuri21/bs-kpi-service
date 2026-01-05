package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.ProjectId;

import java.util.Objects;

public record KPIAssignmentCommand(
        KPIAssignmentId id,
        KPIId kpiId,
        ProjectId projectId,
        double green,
        double yellow,
        double red
) {
    public KPIAssignmentCommand {
        Objects.requireNonNull(id, "KPIAssignment id must not be null");
        Objects.requireNonNull(kpiId, "KPI id must not be null");
        Objects.requireNonNull(projectId, "Project id must not be null");
    }
}
