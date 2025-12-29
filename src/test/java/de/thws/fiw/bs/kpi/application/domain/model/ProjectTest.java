package de.thws.fiw.bs.kpi.application.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    @Test
    void init_anyArgumentNull_throwsException() {
        ProjectId id = ProjectId.newId();
        Name name = new Name("Project");
        RepoUrl repoUrl = RepoUrl.parse("https://github.com/test/repo");

        assertThrows(NullPointerException.class, () -> new Project(null, name, repoUrl));
        assertThrows(NullPointerException.class, () -> new Project(id, null, repoUrl));
        assertThrows(NullPointerException.class, () -> new Project(id, name, null));
    }
}
