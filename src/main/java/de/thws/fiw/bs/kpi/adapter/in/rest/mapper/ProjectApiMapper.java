package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.project.CreateProjectDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.project.ProjectDTO;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.project.Project;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.project.RepoUrl;
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
                new ProjectId(projectDto.getId()),
                new Name(projectDto.getName()),
                new RepoUrl(projectDto.getRepoUrl())
        );
    }

    public Project toDomainModelByCreate(CreateProjectDTO createProjectDTO) {
        return new Project(
                ProjectId.newId(),
                new Name(createProjectDTO.getName()),
                new RepoUrl(createProjectDTO.getRepoUrl())
        );
    }
}
