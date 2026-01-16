package de.thws.fiw.bs.kpi.application.domain.model.project;

import de.thws.fiw.bs.kpi.application.domain.model.Status;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIEvaluationResult;

import java.util.*;

public class ProjectEvaluationResult {

    private final Project project;
    private final Status status;
    private final KPIEvaluationResult focusKpi;
    private final List<KPIEvaluationResult> allKpis;

    private ProjectEvaluationResult(Project project, Status status, KPIEvaluationResult focusKpi, List<KPIEvaluationResult> allKpis) {
        this.project = project;
        this.status = status;
        this.focusKpi = focusKpi;
        this.allKpis = allKpis;
    }

    public static ProjectEvaluationResult aggregate(Project project, List<KPIEvaluationResult> results) {
        Objects.requireNonNull(project, "Project must not be null");
        Objects.requireNonNull(results, "Results must not be null");

        if (results.isEmpty()) {
            throw new IllegalArgumentException("Cannot evaluate project without KPI results");
        }

        KPIEvaluationResult focus = results.stream()
                .max(new FocusKpiComparator())
                .orElseThrow();

        Status overallStatus = focus.getStatus();

        List<KPIEvaluationResult> sortedResults = new ArrayList<>(results);
        sortedResults.sort(new FocusKpiComparator().reversed());

        return new ProjectEvaluationResult(project, overallStatus, focus, sortedResults);
    }

    public Project getProject() {
        return project;
    }

    public Status getStatus() {
        return status;
    }

    public KPIEvaluationResult getFocusKpi() {
        return focusKpi;
    }

    public List<KPIEvaluationResult> getAllKpis() {
        return Collections.unmodifiableList(allKpis);
    }

    private static class FocusKpiComparator implements Comparator<KPIEvaluationResult> {
        @Override
        public int compare(KPIEvaluationResult o1, KPIEvaluationResult o2) {
            int statusComparison = o1.getStatus().compareTo(o2.getStatus());
            if (statusComparison != 0) {
                return statusComparison;
            }
            return o1.getEntry().getTimestamp().compareTo(o2.getEntry().getTimestamp());
        }
    }
}