package de.thws.fiw.bs.kpi.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KPITest {

    private final UUID id = UUID.randomUUID();

    @Test
    void shouldCreateKpiWithValidData() {
        TargetDestination destination = TargetDestination.DECREASING;
        KPI kpi = new KPI(id, "Demo", destination);

        assertEquals(id, kpi.getId());
        assertEquals("Demo", kpi.getName());
        assertEquals(destination, kpi.getDestination());
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new KPI(UUID.randomUUID(), null, TargetDestination.INCREASING)
        );

        assertEquals("Name must not be empty", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new KPI(UUID.randomUUID(), "   ", TargetDestination.RANGE)
        );

        assertEquals("Name must not be empty", ex.getMessage());
    }
}