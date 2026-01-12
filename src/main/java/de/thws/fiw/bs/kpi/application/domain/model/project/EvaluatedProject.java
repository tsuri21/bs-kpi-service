package de.thws.fiw.bs.kpi.application.domain.model.project;

import de.thws.fiw.bs.kpi.application.domain.model.kpi.EvaluatedKPI;
import de.thws.fiw.bs.kpi.application.domain.model.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EvaluatedProject extends Project {

    private final Status status;
    private final EvaluatedKPI focusKpi;
    private List<EvaluatedKPI> evaluatedKPIs = new ArrayList<>();

    public EvaluatedProject(Project project, Status status, EvaluatedKPI focusKpi, List<EvaluatedKPI> evaluatedKPIs) {
        super(project.getId(), project.getName(), project.getRepoUrl());
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.focusKpi = Objects.requireNonNull(focusKpi, "Focus KPI must not be null");
        this.evaluatedKPIs = Objects.requireNonNull(evaluatedKPIs, "Evaluated KPIs must not be null");
    }

    public Status getStatus() {
        return status;
    }

    public EvaluatedKPI getFocusKpi() {
        return focusKpi;
    }

    public List<EvaluatedKPI> getEvaluatedKPIs() {
        return evaluatedKPIs;
    }
}
