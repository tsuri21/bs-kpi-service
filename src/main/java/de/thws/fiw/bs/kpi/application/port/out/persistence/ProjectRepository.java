package de.thws.fiw.bs.kpi.application.port.out.persistence;

import de.thws.fiw.bs.kpi.application.domain.model.Project;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ProjectRepository {
    UUID create(Project p);
    Optional<Project> findById(UUID id);
    Optional<Project> findByUrl(URI repoUrl);
    List<Project> findAll();
    boolean existsById(UUID id);
    void update(Project p);
    void deleteById(UUID id);
}
