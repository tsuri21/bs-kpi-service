package de.thws.fiw.bs.kpi.domain.model;

import java.util.UUID;

public class KPIAssignment {
    private UUID id;
    private double green;
    private double yellow;
    private double red;

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
}
