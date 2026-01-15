package de.thws.fiw.bs.kpi.application.domain.model.project;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class RepoUrlTest {

    @Test
    void parse_nullUrl_throwsException() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> RepoUrl.parse(null)
        );

        assertEquals("Repository URL must not be null", ex.getMessage());
    }

    @Test
    void parse_invalidUri_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> RepoUrl.parse("not a uri")
        );

        assertEquals("Repository URL is not a valid URI", ex.getMessage());
    }

    @Test
    void init_unsupportedScheme_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new RepoUrl(URI.create("ftp://github.com/test/repo"))
        );

        assertEquals("Repository URL must use http or https", ex.getMessage());
    }

    @Test
    void init_missingHost_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new RepoUrl(URI.create("https:///test/repo"))
        );

        assertEquals("Repository URL must contain a host", ex.getMessage());
    }

    @Test
    void init_missingPath_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new RepoUrl(URI.create("https://github.com"))
        );

        assertEquals("Repository URL must contain a path", ex.getMessage());
    }

    @Test
    void parse_validHttpsUrl_returnsRepoUrl() {
        RepoUrl repoUrl = RepoUrl.parse("https://github.com/test/repo");

        assertEquals(URI.create("https://github.com/test/repo"), repoUrl.value());
    }
}
