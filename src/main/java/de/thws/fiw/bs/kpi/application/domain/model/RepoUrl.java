package de.thws.fiw.bs.kpi.application.domain.model;

import java.net.URI;
import java.util.Objects;

public record RepoUrl(URI url) {
    public RepoUrl {
        validateRepoUrl(url);
    }

    public static RepoUrl parse(String url) {
        Objects.requireNonNull(url, "Repository URL must not be null");
        try {
            return new RepoUrl(URI.create(url));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Repository URL is not a valid URI", e);
        }
    }

    private static void validateRepoUrl(URI url) {
        Objects.requireNonNull(url, "Repository URL must not be null");

        String scheme = requireNotBlank(url.getScheme(), "Repository URL must have a scheme");
        if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
            throw new IllegalArgumentException("Repository URL must use http or https");
        }

        requireNotBlank(url.getHost(), "Repository URL must contain a host");

        String path = url.getPath();
        if (path == null || path.isBlank() || "/".equals(path)) {
            throw new IllegalArgumentException("Repository URL must contain a path");
        }
    }

    private static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
