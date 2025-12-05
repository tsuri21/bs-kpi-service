package de.thws.fiw.bs.kpi.domain.model;

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
        TargetDestination destination = kpi.getDestination();
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

    public KPI getKpi() {
        return kpi;
    }

    public void setKpi(KPI kpi) {
        this.kpi = kpi;
    }

    public List<KPIEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<KPIEntry> entries) {
        this.entries = entries;
    }
}
