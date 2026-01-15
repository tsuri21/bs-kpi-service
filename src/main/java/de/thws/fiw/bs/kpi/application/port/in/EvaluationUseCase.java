package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIEvaluationResult;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectEvaluationResult;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;

public interface EvaluationUseCase {

    KPIEvaluationResult evaluateKPI(KPIAssignmentId id);

    ProjectEvaluationResult evaluateProject(ProjectId id);
}
