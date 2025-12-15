package de.thws.fiw.bs.kpi.application.domain.model;

import java.util.List;
import java.util.UUID;

public class KPIAssignment {
    private UUID id;
    private double green;
    private double yellow;
    private double red;
    private KPI kpi;
    private List<KPIEntry> entries;

    public KPIAssignment() {
    }

    public KPIAssignment(UUID id, double green, double yellow, double red, KPI kpi, List<KPIEntry> entries) {
        this.id = id;
        this.kpi = validateKpi(kpi);
        validateAndSetThresholds(green, yellow, red);
        this.entries = normalizeEntries(entries);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public double getGreen() {
        return green;
    }

    public double getYellow() {
        return yellow;
    }

    public double getRed() {
        return red;
    }

    public void setThresholds(double green, double yellow, double red) {
        validateAndSetThresholds(green, yellow, red);
    }

    public KPI getKpi() {
        return kpi;
    }

    public void setKpi(KPI kpi) {
        this.kpi = validateKpi(kpi);
    }

    public List<KPIEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<KPIEntry> entries) {
        this.entries = normalizeEntries(entries);
    }

    private static KPI validateKpi(KPI kpi) {
        if (kpi == null) {
            throw new IllegalArgumentException("KPI must not be null");
        }
        return kpi;
    }

    private static List<KPIEntry> normalizeEntries(List<KPIEntry> entries) {
        return (entries == null) ? List.of() : entries;
    }

    private void validateAndSetThresholds(double green, double yellow, double red) {
        if (kpi == null) {
            throw new IllegalStateException("KPI must be set before thresholds");
        }
        TargetDestination destination = kpi.getDestination();
        if (destination == null) {
            throw new IllegalStateException("KPI destination must not be null");
        }

        boolean result = switch (destination) {
            case INCREASING -> green > yellow && yellow > red;
            case DECREASING -> green < yellow && yellow < red;
            case RANGE -> 0 < yellow && yellow < red;
        };
        if (!result) {
            throw new IllegalArgumentException("Thresholds are not valid");
        }
        this.green = green;
        this.yellow = yellow;
        this.red = red;
    }
}