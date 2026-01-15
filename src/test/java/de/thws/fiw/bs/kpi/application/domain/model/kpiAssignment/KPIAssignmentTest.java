package de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment;

import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.Thresholds;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KPIAssignmentTest {

    @Test
    void init_anyArgumentNull_throwsException() {
        KPIAssignmentId id = KPIAssignmentId.newId();
        Thresholds thresholds = Thresholds.linear(TargetDestination.INCREASING, 50, 30);
        KPI kpi = new KPI(KPIId.newId(), new Name("Test"), TargetDestination.DECREASING);
        ProjectId projectId = ProjectId.newId();

        assertThrows(NullPointerException.class, () -> new KPIAssignment(null, thresholds, kpi, projectId));
        assertThrows(NullPointerException.class, () -> new KPIAssignment(id, null, kpi, projectId));
        assertThrows(NullPointerException.class, () -> new KPIAssignment(id, thresholds, null, projectId));
        assertThrows(NullPointerException.class, () -> new KPIAssignment(id, thresholds, kpi, null));
    }
}
