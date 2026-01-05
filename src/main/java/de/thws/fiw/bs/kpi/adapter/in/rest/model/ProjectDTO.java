package de.thws.fiw.bs.kpi.adapter.in.rest.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.net.URI;
import java.util.UUID;

public class ProjectDTO extends AbstractDTO {
    @NotBlank(message="Name must not be blank")
    private String name;

    @NotNull(message="RepoUrl is required")
    private URI repoUrl;

    public ProjectDTO() {
    }

    public ProjectDTO(UUID id, String name, URI repoUrl) {
        this.id = id;
        this.name = name;
        this.repoUrl = repoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URI getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(URI repoUrl) {
        this.repoUrl = repoUrl;
    }
}
