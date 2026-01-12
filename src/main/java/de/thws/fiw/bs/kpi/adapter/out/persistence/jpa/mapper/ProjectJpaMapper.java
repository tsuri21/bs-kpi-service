package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.ProjectEntity;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.project.Project;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.project.RepoUrl;
import jakarta.inject.Singleton;

@Singleton
public class ProjectJpaMapper implements PersistenceMapper<Project, ProjectEntity> {

    @Override
    public Project toDomainModel(ProjectEntity entity) {
        return new Project(
                new ProjectId(entity.getId()),
                new Name(entity.getName()),
                new RepoUrl(entity.getRepoUrl())
        );
    }

    @Override
    public ProjectEntity toPersistenceModel(Project project) {
        return new ProjectEntity(
                project.getId().value(),
                project.getName().value(),
                project.getRepoUrl().value()
        );
    }
}