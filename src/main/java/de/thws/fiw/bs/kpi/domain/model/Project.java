package de.thws.fiw.bs.kpi.domain.model;

import java.net.URI;
import java.util.UUID;

public class Project {
    private UUID id;
    private String name;
    private URI repoUrl;


    public Project(UUID id, String name, URI repoUrl) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        this.id = id;
        this.name = validateName(name);
        this.repoUrl = validateRepoUrl(repoUrl);
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public URI repoUrl() {
        return repoUrl;
    }

    public void rename(String newName) {
        this.name = validateName(newName);
    }

    public void changeRepoUrl(URI newRepoUrl) {
        this.repoUrl = validateRepoUrl(newRepoUrl);
    }

    private String validateName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        return trimmedName;
    }

    private URI validateRepoUrl(URI repoUrl) {
        if (repoUrl == null) {
            throw new IllegalArgumentException("Repository URL cannot be null");
        }
        String scheme = repoUrl.getScheme();
        if (scheme == null) throw new IllegalArgumentException("Repository URL must have a scheme");
        String s = scheme.toLowerCase();
        if (!s.equals("https") && !s.equals("http"))
            throw new IllegalArgumentException("Repository URL must use http or https");
        if (repoUrl.getHost() == null || repoUrl.getHost().isBlank())
            throw new IllegalArgumentException("Repository URL must contain a host");
        String path = repoUrl.getPath();
        if (path == null || path.isBlank() || "/".equals(path))
            throw new IllegalArgumentException("Repository URL must contain a path");
        return repoUrl;
    }
}
