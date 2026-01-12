package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;

import java.util.Optional;

public interface KPIAssignmentUseCase {
    Optional<KPIAssignment> readById(KPIAssignmentId id);

    Page<KPIAssignment> readAll(KPIId kpiId, ProjectId projectId, PageRequest pageRequest);

    void create(KPIAssignmentCommand kpiAssignmentCmd);

    void update(KPIAssignmentCommand kpiAssignmentCmd);

    void delete(KPIAssignmentId id);
}
