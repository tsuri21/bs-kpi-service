package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;

import java.util.Optional;

public interface KPIAssignmentUseCase {
    Optional<KPIAssignment> readById(KPIAssignmentId id);

    Page<KPIAssignment> readAll(KPIId kpiId, ProjectId projectId, PageRequest pageRequest);

    void create(KPIAssignment kpiAssignment);

    void update(KPIAssignment kpiAssignment);

    void delete(KPIAssignmentId id);
}
