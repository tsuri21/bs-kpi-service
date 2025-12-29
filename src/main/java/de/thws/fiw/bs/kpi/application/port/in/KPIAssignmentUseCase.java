package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;

public interface KPIAssignmentUseCase {
    KPIAssignment readById(KPIAssignmentId id);

    Page<KPIAssignment> readAll(KPIId kpiId, ProjectId projectId, PageRequest pageRequest);

    void create(KPIAssignment kpiAssignment);

    void update(KPIAssignmentId id, KPIAssignment kpiAssignment);

    void delete(KPIAssignmentId id);
}
