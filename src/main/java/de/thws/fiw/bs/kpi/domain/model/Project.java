package de.thws.fiw.bs.kpi.domain.model;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public class Project {
    private UUID id;
    private String name;
    private URI repoUrl;
    private List<KPIAssignment> assignments;

    public Project() {
    }

    public Project(UUID id, String name, URI repoUrl, List<KPIAssignment> assignments) {
        this.id = id;
        setName(name);
        setRepoUrl(repoUrl);
        setAssignments(assignments);
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

    public final void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        this.name = name;
    }

    public URI getRepoUrl() {
        return repoUrl;
    }

    public final void setRepoUrl(URI repoUrl) {
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
        this.repoUrl = repoUrl;
    }

    public List<KPIAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<KPIAssignment> assignments) {
        this.assignments = (assignments == null) ? List.of() : assignments;
    }
}
