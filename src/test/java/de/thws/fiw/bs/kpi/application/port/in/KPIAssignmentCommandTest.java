package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KPIAssignmentCommandTest {

    @Test
    void init_anyArgumentNull_throwsException() {
        KPIAssignmentId id = KPIAssignmentId.newId();
        KPIId kpiId = KPIId.newId();
        ProjectId projectId = ProjectId.newId();
        double green = 20, yellow = 10, targetValue = 5;

        assertThrows(NullPointerException.class, () -> new KPIAssignmentCommand(null, kpiId, projectId, green, yellow, targetValue));
        assertThrows(NullPointerException.class, () -> new KPIAssignmentCommand(id, null, projectId, green, yellow, targetValue));
        assertThrows(NullPointerException.class, () -> new KPIAssignmentCommand(id, kpiId, null, green, yellow, targetValue));
    }
}