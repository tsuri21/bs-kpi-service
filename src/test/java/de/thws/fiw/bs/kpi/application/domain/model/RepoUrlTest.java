package de.thws.fiw.bs.kpi.application.domain.model;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class RepoUrlTest {

    @Test
    void rejectsNullUrlWhenParsing() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> RepoUrl.parse(null)
        );

        assertEquals("Repository URL must not be null", ex.getMessage());
    }

    @Test
    void rejectsInvalidUriWhenParsing() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> RepoUrl.parse("not a uri")
        );

        assertEquals("Repository URL is not a valid URI", ex.getMessage());
    }

    @Test
    void rejectsUnsupportedScheme() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new RepoUrl(URI.create("ftp://github.com/test/repo"))
        );

        assertEquals("Repository URL must use http or https", ex.getMessage());
    }

    @Test
    void rejectsMissingHost() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new RepoUrl(URI.create("https:///test/repo"))
        );

        assertEquals("Repository URL must contain a host", ex.getMessage());
    }

    @Test
    void rejectsMissingPath() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new RepoUrl(URI.create("https://github.com"))
        );

        assertEquals("Repository URL must contain a path", ex.getMessage());
    }

    @Test
    void createsRepoUrlForValidHttpsUrl() {
        RepoUrl repoUrl = RepoUrl.parse("https://github.com/test/repo");

        assertEquals(URI.create("https://github.com/test/repo"), repoUrl.value());
    }
}
