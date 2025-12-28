package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.ProjectEntity;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.Project;
import de.thws.fiw.bs.kpi.application.domain.model.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.RepoUrl;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ProjectMapper {
    public ProjectEntity toPersistenceModel(Project project) {
        return new ProjectEntity(
                project.getId().value(),
                project.getName().value(),
                project.getRepoUrl().value()
        );
    }

    public List<Project> toDomainModels(List<ProjectEntity> projectEntities) {
        return projectEntities.stream().map(this::toDomainModel).collect(Collectors.toList());
    }

    public Project toDomainModel(ProjectEntity projectEntity) {
        return new Project(
                new ProjectId(projectEntity.getId()),
                new Name(projectEntity.getName()),
                new RepoUrl(projectEntity.getRepoUrl())
        );
    }
}
