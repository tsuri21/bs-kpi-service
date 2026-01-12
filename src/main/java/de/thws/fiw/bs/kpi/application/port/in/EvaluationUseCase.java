package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.kpi.EvaluatedKPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.project.EvaluatedProject;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;

public interface EvaluationUseCase {

    EvaluatedKPI evaluateKPI(KPIAssignmentId id);

    EvaluatedProject evaluateProject(ProjectId id);
}
