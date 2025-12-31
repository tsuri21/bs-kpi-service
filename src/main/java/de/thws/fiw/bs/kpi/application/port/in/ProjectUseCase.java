package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.Project;
import de.thws.fiw.bs.kpi.application.domain.model.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.RepoUrl;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;

import java.util.Optional;

public interface ProjectUseCase {
    Optional<Project> readById(ProjectId id);

    Page<Project> readAll(Name name, RepoUrl repoUrl, PageRequest pageRequest);

    void create(Project project);

    void update(Project project);

    void delete(ProjectId id);
}
