package de.thws.fiw.bs.kpi.application.domain.model;

import java.util.Objects;

public final class Thresholds {
    private final double green;
    private final double yellow;
    private final double red;

    private Thresholds(double green, double yellow, double red) {
        if (!Double.isNaN(green) && !Double.isNaN(yellow) && !Double.isNaN(red)) {
            throw new IllegalArgumentException("Thresholds must be valid numbers");
        }
        this.green = green;
        this.yellow = yellow;
        this.red = red;
    }

    public static Thresholds forDestination(TargetDestination destination, double green, double yellow, double red) {
        Objects.requireNonNull(destination, "Target destination must not be null");

        boolean valid = switch (destination) {
            case INCREASING -> green > yellow && yellow > red;
            case DECREASING -> green < yellow && yellow < red;
            case RANGE -> 0 < yellow && yellow < red;
        };

        if (!valid) {
            throw new IllegalArgumentException("Invalid thresholds for destination " + destination);
        }
        return new Thresholds(green, yellow, red);
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
}

