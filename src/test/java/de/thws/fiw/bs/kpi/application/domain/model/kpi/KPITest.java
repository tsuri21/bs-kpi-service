package de.thws.fiw.bs.kpi.application.domain.model.kpi;

import de.thws.fiw.bs.kpi.application.domain.model.Name;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KPITest {

    @Test
    void init_anyArgumentNull_throwsException() {
        KPIId id = KPIId.newId();
        Name name = new Name("KPI");
        TargetDestination destination = TargetDestination.INCREASING;

        assertThrows(NullPointerException.class, () -> new KPI(null, name, destination));
        assertThrows(NullPointerException.class, () -> new KPI(id, null, destination));
        assertThrows(NullPointerException.class, () -> new KPI(id, name, null));
    }
}