package de.thws.fiw.bs.kpi.application.domain.model;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    private final UUID id = UUID.randomUUID();

    @Test
    void shouldCreateProjectWithValidData() {
        Project project = new Project(
                id,
                "Project",
                URI.create("https://github.com/test/repo"),
                null
        );

        assertEquals(id, project.getId());
        assertEquals("Project", project.getName());
        assertEquals(URI.create("https://github.com/test/repo"), project.getRepoUrl());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Project(id, "   ", URI.create("https://github.com/test/repo"), null)
        );

        assertEquals("Name must not be empty", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Project(id, null, URI.create("https://github.com/test/repo"), null)
        );

        assertEquals("Name must not be empty", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenRepoUrlIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Project(id, "Project", null, null)
        );

        assertEquals("Repository URL cannot be null", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenRepoUrlIsNotHttp() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Project(id, "Project", URI.create("ftp://github.com/test/repo"), null)
        );

        assertEquals("Repository URL must use http or https", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenRepoUrlHasNoHost() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Project(id, "Project", URI.create("https:///test/repo"), null)
        );

        assertEquals("Repository URL must contain a host", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenRepoUrlHasNoPath() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Project(id, "Project", URI.create("https://github.com"), null)
        );

        assertEquals("Repository URL must contain a path", ex.getMessage());
    }
}
