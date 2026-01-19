package de.thws.fiw.bs.kpi.adapter.in.rest.model.project;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.AbstractDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.net.URI;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProjectDTO that = (ProjectDTO) o;
        return Objects.equals(name, that.name) && Objects.equals(repoUrl, that.repoUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, repoUrl);
    }
}
