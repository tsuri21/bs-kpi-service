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
    void shouldThrowExceptionWhenDestinationIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new KPI(id, "Demo", null)
        );

        assertEquals("Destination must not be null", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new KPI(id, null, TargetDestination.INCREASING)
        );

        assertEquals("Name must not be empty", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new KPI(id, "   ", TargetDestination.RANGE)
        );

        assertEquals("Name must not be empty", ex.getMessage());
    }
}