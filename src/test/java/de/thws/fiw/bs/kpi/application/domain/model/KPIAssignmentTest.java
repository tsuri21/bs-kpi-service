package de.thws.fiw.bs.kpi.application.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KPIAssignmentTest {

    @Test
    void init_anyArgumentNull_throwsException() {
        KPIAssignmentId id = KPIAssignmentId.newId();
        Thresholds thresholds = Thresholds.forDestination(TargetDestination.INCREASING, 50, 30, 15);
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();

        assertThrows(NullPointerException.class, () -> new KPIAssignment(null, thresholds, kpiId, projectId));
        assertThrows(NullPointerException.class, () -> new KPIAssignment(id, null, kpiId, projectId));
        assertThrows(NullPointerException.class, () -> new KPIAssignment(id, thresholds, null, projectId));
        assertThrows(NullPointerException.class, () -> new KPIAssignment(id, thresholds, kpiId, null));
    }
}
