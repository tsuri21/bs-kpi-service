package de.thws.fiw.bs.kpi.application.domain.model;

import de.thws.fiw.bs.kpi.application.domain.model.kpi.TargetDestination;

import java.util.Objects;

public final class Thresholds {

    private final double green;
    private final double yellow;
    private final Double targetValue;

    private Thresholds(double green, double yellow, Double targetValue) {
        if (Double.isNaN(green) || Double.isNaN(yellow)) {
            throw new IllegalArgumentException("Threshold values must be valid numbers");
        }
        if (targetValue != null && Double.isNaN(targetValue)) {
            throw new IllegalArgumentException("Target value must be a valid number");
        }
        this.green = green;
        this.yellow = yellow;
        this.targetValue = targetValue;
    }

    public static Thresholds linear(TargetDestination destination, double green, double yellow) {
        Objects.requireNonNull(destination, "Destination must not be null");
        if (destination == TargetDestination.RANGE) {
            throw new IllegalArgumentException("Use range() factory for RANGE destination");
        }

        boolean valid = switch (destination) {
            case INCREASING -> green > yellow;
            case DECREASING -> green < yellow;
            default -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException("Incorrect order of tolerances for the destination " + destination);
        }

        return new Thresholds(green, yellow, null);
    }

    public static Thresholds range(double targetValue, double green, double yellow) {
        if (Math.abs(targetValue) < 0.00001) {
            throw new IllegalArgumentException("Target value must not be zero");
        }

        if (green <= 0 || yellow <= 0) {
            throw new IllegalArgumentException("Tolerances must be positive");
        }

        if (green >= yellow) {
            throw new IllegalArgumentException("Green tolerance must be smaller than yellow tolerance");
        }

        return new Thresholds(green, yellow, targetValue);
    }

    public double getGreen() { return green; }

    public double getYellow() { return yellow; }

    public Double getTargetValue() { return targetValue; }
}

