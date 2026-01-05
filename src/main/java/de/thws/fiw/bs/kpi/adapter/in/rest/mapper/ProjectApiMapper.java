package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.ProjectDTO;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.Project;
import de.thws.fiw.bs.kpi.application.domain.model.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.RepoUrl;
import jakarta.inject.Singleton;

@Singleton
public class ProjectApiMapper implements ApiMapper<Project, ProjectDTO> {

    @Override
    public ProjectDTO toApiModel(Project project) {
        return new ProjectDTO(
                project.getId().value(),
                project.getName().value(),
                project.getRepoUrl().value()
        );
    }

    @Override
    public Project toDomainModel(ProjectDTO projectDto) {
        return new Project(
                projectDto.getId() == null ? ProjectId.newId() : new ProjectId(projectDto.getId()),
                new Name(projectDto.getName()),
                new RepoUrl(projectDto.getRepoUrl())
        );
    }
}
