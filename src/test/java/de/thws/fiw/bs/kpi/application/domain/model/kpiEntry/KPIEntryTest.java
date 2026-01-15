package de.thws.fiw.bs.kpi.application.domain.model.kpiEntry;

import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class KPIEntryTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void init_anyArgumentNull_throwsException() {
        KPIEntryId id = KPIEntryId.newId();
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        Instant ts = Instant.parse("2025-01-01T09:59:00Z");

        assertThrows(NullPointerException.class,
                () -> KPIEntry.createNew(null, assignmentId, ts, 10.0, FIXED_CLOCK));
        assertThrows(NullPointerException.class,
                () -> KPIEntry.createNew(id, null, ts, 10.0, FIXED_CLOCK));
        assertThrows(NullPointerException.class,
                () -> KPIEntry.createNew(id, assignmentId, null, 10.0, FIXED_CLOCK));
        assertThrows(NullPointerException.class,
                () -> KPIEntry.createNew(id, assignmentId, ts, 10.0, null));
    }

    @Test
    void init_timestampInFuture_throwsException() {
        KPIEntryId id = KPIEntryId.newId();
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        Instant future = Instant.parse("2025-01-01T10:00:01Z");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> KPIEntry.createNew(id, assignmentId, future, 10.0, FIXED_CLOCK)
        );

        assertEquals("Timestamp must not be in the future", ex.getMessage());
    }

    @Test
    void init_validTimestamp_success() {
        KPIEntryId id = KPIEntryId.newId();
        KPIAssignmentId assignmentId = KPIAssignmentId.newId();
        Instant past = Instant.parse("2025-01-01T09:59:00Z");

        KPIEntry entry = KPIEntry.createNew(id, assignmentId, past, 42.5, FIXED_CLOCK);

        assertEquals(id, entry.getId());
        assertEquals(assignmentId, entry.getKpiAssignmentId());
        assertEquals(past, entry.getTimestamp());
        assertEquals(42.5, entry.getValue());
    }
}
