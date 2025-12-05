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

    public KPIAssignment() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public double getGreen() {
        return green;
    }

    public void setGreen(double green) {
        this.green = green;
    }

    public double getYellow() {
        return yellow;
    }

    public void setYellow(double yellow) {
        this.yellow = yellow;
    }

    public double getRed() {
        return red;
    }

    public void setRed(double red) {
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
