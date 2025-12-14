package de.thws.fiw.bs.kpi.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KPIEntryTest {

    private final UUID id = UUID.randomUUID();

    @Test
    void shouldCreateKpiEntryWithValidData() {
        LocalDateTime timestamp = LocalDateTime.now().minusMinutes(1);
        double value = 42.5;

        KPIEntry entry = new KPIEntry(id, timestamp, value);

        assertEquals(id, entry.getId());
        assertEquals(timestamp, entry.getTimestamp());
        assertEquals(value, entry.getValue());
    }

    @Test
    void shouldThrowExceptionWhenTimestampIsInFuture() {
        LocalDateTime futureTimestamp = LocalDateTime.now().plusMinutes(1);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new KPIEntry(id, futureTimestamp, 10.0)
        );

        assertEquals("Timestamp must not be in the future", ex.getMessage());
    }
}
