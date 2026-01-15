package de.thws.fiw.bs.kpi.application.domain.model.kpi;

import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.Status;
import de.thws.fiw.bs.kpi.application.domain.model.Thresholds;

import java.util.Objects;

public class EvaluatedKPI extends KPI {

    private final Status status;

    private EvaluatedKPI(KPI kpi, Status status) {
        super(kpi.getId(), kpi.getName(), kpi.getDestination());
        this.status = Objects.requireNonNull(status, "Status must not be null");
    }

    public static EvaluatedKPI evaluateKPI(KPI kpi, Thresholds threshold, KPIEntry latestEntry) {
        Objects.requireNonNull(kpi, "KPI must not be null");
        Objects.requireNonNull(threshold, "Thresholds must not be null");
        Objects.requireNonNull(latestEntry, "KPIEntry must not be null");

        double value = latestEntry.getValue();
        TargetDestination destination = kpi.getDestination();
        Status status;

        switch (destination) {
            case INCREASING -> {
                if (value > threshold.getGreen()) status = Status.GREEN;
                else if (value > threshold.getYellow()) status = Status.YELLOW;
                else status = Status.RED;
            }
            case DECREASING -> {
                if (value < threshold.getGreen()) status = Status.GREEN;
                else if (value < threshold.getYellow()) status = Status.YELLOW;
                else status = Status.RED;
            }
            case RANGE -> {
                if (value < threshold.getGreen() + threshold.getGreen() * threshold.getYellow() &&
                        value > threshold.getGreen() - threshold.getGreen() * threshold.getYellow()) status = Status.GREEN;
                else if (value < threshold.getGreen() + threshold.getGreen() * threshold.getRed() &&
                        value > threshold.getGreen() - threshold.getGreen() * threshold.getRed()) status = Status.YELLOW;
                else status = Status.RED;
            }
            default -> status = Status.RED;
        }

        return new EvaluatedKPI(kpi, status);
    }

    public Status getStatus() {
        return status;
    }
}
