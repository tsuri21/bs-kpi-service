package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.EvaluationException;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.EvaluatedKPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.project.EvaluatedProject;
import de.thws.fiw.bs.kpi.application.domain.model.project.Project;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.EvaluationUseCase;
import de.thws.fiw.bs.kpi.application.port.out.KPIAssignmentRepository;
import de.thws.fiw.bs.kpi.application.port.out.KPIEntryRepository;
import de.thws.fiw.bs.kpi.application.port.out.ProjectRepository;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class EvaluationService implements EvaluationUseCase {

    @Inject
    KPIAssignmentRepository kpiAssignmentRepository;

    @Inject
    KPIEntryRepository kpiEntryRepository;

    @Inject
    ProjectRepository projectRepository;

    @Override
    public EvaluatedKPI evaluateKPI(KPIAssignmentId id) {
        KPIAssignment kpiAssignment = kpiAssignmentRepository.findById(id)
                .orElseThrow(() -> new EvaluationException("KPIAssignment could not be found"));

        KPIEntry kpiEntry = kpiEntryRepository.findLatest(id)
                .orElseThrow(() -> new EvaluationException("KPI has no entries"));

        return EvaluatedKPI.evaluateKPI(kpiAssignment.getKpi(), kpiAssignment.getThresholds(), kpiEntry);
    }

    @Override
    public EvaluatedProject evaluateProject(ProjectId id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EvaluationException("Project could not be found"));

        Page<KPIAssignment> kpiAssignmentsPage = kpiAssignmentRepository.findByFilter(null, id, new PageRequest(0, 100));
        List<KPIAssignment> kpiAssignments = kpiAssignmentsPage.content();
        if (kpiAssignments.isEmpty()) {
            throw new EvaluationException("Project has no KPI assignments");
        }

        List<EvaluatedKPI> evaluatedKPIs = new ArrayList<>();
        EvaluatedKPI focusKpi = null;
        Instant focusTimestamp = null;

        for (KPIAssignment kpiAssignment : kpiAssignments) {
            EvaluatedKPI kpi = evaluateKPI(kpiAssignment.getId());
            KPIEntry kpiEntry = kpiEntryRepository.findLatest(kpiAssignment.getId())
                    .orElseThrow(() -> new EvaluationException("KPI with id " + kpi.getId() + " has no entries"));

            if (focusKpi == null) {
                focusKpi = kpi;
                focusTimestamp = kpiEntry.getTimestamp();
            } else if (kpi.getStatus().compareTo(focusKpi.getStatus()) > 0) {
                focusKpi = kpi;
                focusTimestamp = kpiEntry.getTimestamp();
            } else if (kpi.getStatus().equals(focusKpi.getStatus()) && kpiEntry.getTimestamp().isAfter(focusTimestamp)) {
                focusKpi = kpi;
                focusTimestamp = kpiEntry.getTimestamp();
            }
            evaluatedKPIs.add(evaluateKPI(kpiAssignment.getId()));
        }
        return new EvaluatedProject(project, focusKpi.getStatus(), focusKpi, evaluatedKPIs);
    }
}
