package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.EvaluationException;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIEvaluationResult;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectEvaluationResult;
import de.thws.fiw.bs.kpi.application.domain.model.project.Project;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.EvaluationUseCase;
import de.thws.fiw.bs.kpi.application.port.out.KPIAssignmentRepository;
import de.thws.fiw.bs.kpi.application.port.out.KPIEntryRepository;
import de.thws.fiw.bs.kpi.application.port.out.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EvaluationService implements EvaluationUseCase {

    @Inject
    KPIAssignmentRepository kpiAssignmentRepository;

    @Inject
    KPIEntryRepository kpiEntryRepository;

    @Inject
    ProjectRepository projectRepository;

    @Override
    public KPIEvaluationResult evaluateKPI(KPIAssignmentId id) {
        KPIAssignment kpiAssignment = kpiAssignmentRepository.findById(id)
                .orElseThrow(() -> new EvaluationException("KPIAssignment could not be found"));

        KPIEntry kpiEntry = kpiEntryRepository.findLatest(id)
                .orElseThrow(() -> new EvaluationException("KPI has no entries"));

        return KPIEvaluationResult.evaluate(kpiAssignment, kpiEntry);
    }

    @Override
    public ProjectEvaluationResult evaluateProject(ProjectId id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EvaluationException("Project could not be found"));

        Page<KPIAssignment> kpiAssignmentsPage = kpiAssignmentRepository.findByFilter(null, id, new PageRequest(0, 100));
        List<KPIAssignment> kpiAssignments = kpiAssignmentsPage.content();
        if (kpiAssignments.isEmpty()) {
            throw new EvaluationException("Project has no KPI assignments");
        }

        Map<KPIAssignmentId, KPIEntry> latestEntries = kpiEntryRepository.findLatestEntriesByProject(id);
        List<KPIEvaluationResult> results = new ArrayList<>();

        for (KPIAssignment assignment : kpiAssignments) {
            KPIEntry entry = latestEntries.get(assignment.getId());

            // TODO: discuss if in this case the assignment should be skipped or an exception should be thrown
            //  -> I thought this was specified in out project specification but is is not
            if (entry == null) {
                throw new EvaluationException("KPI Assignment " + assignment.getId() + " has no entries");
            }
            results.add(KPIEvaluationResult.evaluate(assignment, entry));
        }
        return ProjectEvaluationResult.aggregate(project, results);
    }
}
