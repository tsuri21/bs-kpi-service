package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;

import java.net.URI;
import java.util.UUID;

@Entity
public class ProjectEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private URI repoUrl;

    public ProjectEntity() {}

    public ProjectEntity(UUID id, String name, URI repoUrl) {
        this.id = id;
        this.name = name;
        this.repoUrl = repoUrl;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
