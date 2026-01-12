package de.thws.fiw.bs.kpi.application.port.out;

import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.project.Project;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.project.RepoUrl;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;

import java.util.Optional;

public interface ProjectRepository {
    Optional<Project> findById(ProjectId id);

    Page<Project> findByFilter(Name name, RepoUrl repoUrl, PageRequest pageRequest);

    void save(Project project);

    void update(Project project);

    void delete(ProjectId id);
}
