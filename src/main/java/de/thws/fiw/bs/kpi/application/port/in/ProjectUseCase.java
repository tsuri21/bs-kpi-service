package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.Project;
import de.thws.fiw.bs.kpi.application.domain.model.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.RepoUrl;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;

public interface ProjectUseCase {
    Project readById(ProjectId id);

    Page<Project> readAll(RepoUrl repoUrl, Name name, PageRequest pageRequest);

    void create(Project project);

    void update(ProjectId id, Project project);

    void delete(ProjectId id);
}
