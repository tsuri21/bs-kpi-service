package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.project.Project;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.project.RepoUrl;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.ProjectUseCase;
import de.thws.fiw.bs.kpi.application.port.out.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class ProjectService implements ProjectUseCase {

    @Inject
    ProjectRepository projectRepository;

    @Override
    public Optional<Project> readById(ProjectId id) {
        return projectRepository.findById(id);
    }

    @Override
    public Page<Project> readAll(Name name, RepoUrl repoUrl, PageRequest pageRequest) {
        return projectRepository.findByFilter(name, repoUrl, pageRequest);
    }

    @Override
    public void create(Project project) {
        projectRepository.save(project);
    }

    @Override
    public void update(Project project) {
        projectRepository.findById(project.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", project.getId()));

        projectRepository.update(project);
    }

    @Override
    public void delete(ProjectId id) {
        projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        projectRepository.delete(id);
    }
}
