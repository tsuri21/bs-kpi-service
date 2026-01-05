package de.thws.fiw.bs.kpi.common;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ClockProducerTest {

    @Inject
    Clock clock;

    @ConfigProperty(name = "app.timezone", defaultValue = "UTC")
    String expectedZoneId;

    @Test
    void clock_injected_correctTimeZone() {
        assertNotNull(clock);

        assertEquals(ZoneId.of(expectedZoneId), clock.getZone());

        System.out.println(clock.getZone());
        System.out.println(clock.instant());
    }
}