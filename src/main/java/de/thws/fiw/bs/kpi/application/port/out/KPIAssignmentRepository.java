package de.thws.fiw.bs.kpi.application.port.out;

import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;

import java.util.Optional;

public interface KPIAssignmentRepository {
    Optional<KPIAssignment> findById(KPIAssignmentId id);
    Page<KPIAssignment> findByFilter(KPIId kpiId, ProjectId projectId, PageRequest pageRequest);
    void save(KPIAssignment kpiAssignment);
    void update(KPIAssignment kpiAssignment);
    void delete(KPIAssignmentId id);
}
