package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
import de.thws.fiw.bs.kpi.application.domain.model.*;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.KPIAssignmentCommand;
import de.thws.fiw.bs.kpi.application.port.in.KPIAssignmentUseCase;
import de.thws.fiw.bs.kpi.application.port.out.KPIAssignmentRepository;
import de.thws.fiw.bs.kpi.application.port.out.KPIRepository;
import de.thws.fiw.bs.kpi.application.port.out.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class KPIAssignmentService implements KPIAssignmentUseCase {

    @Inject
    KPIAssignmentRepository kpiAssignmentRepository;

    @Inject
    KPIRepository kpiRepository;

    @Inject
    ProjectRepository projectRepository;

    @Override
    public Optional<KPIAssignment> readById(KPIAssignmentId id) {
        return kpiAssignmentRepository.findById(id);
    }

    @Override
    public Page<KPIAssignment> readAll(KPIId kpiId, ProjectId projectId, PageRequest pageRequest) {
        return kpiAssignmentRepository.findByFilter(kpiId, projectId, pageRequest);
    }

    @Override
    public void create(KPIAssignmentCommand kpiAssignmentCmd) {
        KPIAssignment assignment = createKPIAssignment(kpiAssignmentCmd);
        kpiAssignmentRepository.save(assignment);
    }

    @Override
    public void update(KPIAssignmentCommand kpiAssignmentCmd) {
        KPIAssignment assignment = createKPIAssignment(kpiAssignmentCmd);

        kpiAssignmentRepository.findById(assignment.getId())
                .orElseThrow(() -> new ResourceNotFoundException("KPIAssignment", assignment.getId()));

        kpiAssignmentRepository.update(assignment);
    }

    @Override
    public void delete(KPIAssignmentId id) {
        kpiAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KPIAssignment", id));

        kpiAssignmentRepository.delete(id);
    }

    private KPIAssignment createKPIAssignment(KPIAssignmentCommand kpiAssignmentCmd) {
        KPIId kpiId = kpiAssignmentCmd.kpiId();
        ProjectId projectId = kpiAssignmentCmd.projectId();

        KPI kpi = kpiRepository.findById(kpiId).orElseThrow(() -> new ResourceNotFoundException("KPI", kpiId));
        projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        Thresholds thresholds = Thresholds.forDestination(
                kpi.getDestination(),
                kpiAssignmentCmd.green(),
                kpiAssignmentCmd.yellow(),
                kpiAssignmentCmd.red()
        );

        return new KPIAssignment(
                kpiAssignmentCmd.id(),
                thresholds,
                kpi,
                projectId
        );
    }
}
