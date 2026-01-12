package de.thws.fiw.bs.kpi.application.domain.model.project;

import de.thws.fiw.bs.kpi.application.domain.model.Name;

import java.util.Objects;

public class Project {
    private final ProjectId id;
    private final Name name;
    private final RepoUrl repoUrl;

    public Project(ProjectId id, Name name, RepoUrl repoUrl) {
        this.id = Objects.requireNonNull(id, "Project id must not be null");
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.repoUrl = Objects.requireNonNull(repoUrl, "Project repository URL must not be null");
    }

    public ProjectId getId() {
        return id;
    }

    public Name getName() {
        return name;
    }

    public RepoUrl getRepoUrl() {
        return repoUrl;
    }
}
