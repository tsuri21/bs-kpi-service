package de.thws.fiw.bs.kpi.common;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Clock;
import java.time.ZoneId;

@ApplicationScoped
public class ClockProducer {

    @ConfigProperty(name = "app.timezone", defaultValue = "UTC")
    String zoneId;

    @Produces
    @ApplicationScoped
    public Clock clock() {
        return Clock.system(ZoneId.of(zoneId));
    }
}
