package de.thws.fiw.bs.kpi.adapter.in.rest.model.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.net.URI;

public class CreateProjectDTO {

    @NotBlank(message="Name must not be blank")
    private String name;

    @NotNull(message="RepoUrl is required")
    private URI repoUrl;

    public CreateProjectDTO() {
    }

    public CreateProjectDTO(String name, URI repoUrl) {
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
