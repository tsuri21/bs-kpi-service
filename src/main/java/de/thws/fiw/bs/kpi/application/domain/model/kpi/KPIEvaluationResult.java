package de.thws.fiw.bs.kpi.application.domain.model.kpi;

import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.Status;
import de.thws.fiw.bs.kpi.application.domain.model.Thresholds;

import java.util.Objects;

public class KPIEvaluationResult {

    private final KPI kpi;
    private final Status status;
    private final KPIEntry entry;

    private KPIEvaluationResult(KPI kpi, Status status, KPIEntry entry) {
        this.kpi = kpi;
        this.status = status;
        this.entry = entry;
    }

    public static KPIEvaluationResult evaluate(KPIAssignment assignment, KPIEntry entry) {
        Objects.requireNonNull(assignment, "Assignment must not be null");
        Objects.requireNonNull(entry, "Entry must not be null");

        Thresholds thresholds = assignment.getThresholds();
        TargetDestination destination = assignment.getKpi().getDestination();

        Status status = thresholds.calculateStatus(entry.getValue(), destination);

        return new KPIEvaluationResult(assignment.getKpi(), status, entry);
    }

    public KPI getKpi() {
        return kpi;
    }

    public Status getStatus() {
        return status;
    }

    public KPIEntry getEntry() {
        return entry;
    }
}
