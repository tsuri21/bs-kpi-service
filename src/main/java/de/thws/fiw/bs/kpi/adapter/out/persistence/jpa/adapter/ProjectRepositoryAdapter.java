package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.adapter;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.ProjectEntity;
import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper.ProjectMapper;
import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.util.ExceptionUtils;
import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import de.thws.fiw.bs.kpi.application.domain.exception.InfrastructureException;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.Project;
import de.thws.fiw.bs.kpi.application.domain.model.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.RepoUrl;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.out.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProjectRepositoryAdapter implements ProjectRepository {

    @Inject
    ProjectMapper mapper;

    @Inject
    EntityManager em;

    @Override
    public Optional<Project> findById(ProjectId id) {
        try {
            ProjectEntity project = em.find(ProjectEntity.class, id.value());
            return Optional.ofNullable(mapper.toDomainModel(project));
        } catch (PersistenceException ex) {
            throw new InfrastructureException("Database access failed for ID: " + id.value(), ex);
        }
    }

    @Override
    public Page<Project> findByFilter(Name name, RepoUrl repoUrl, PageRequest pageRequest) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            long total = countSearchResults(cb, repoUrl, name);

            if (total == 0) {
                return new Page<>(List.of(), pageRequest, 0);
            }

            List<ProjectEntity> entities = fetchPageResults(cb, repoUrl, name, pageRequest);
            return new Page<>(mapper.toDomainModels(entities), pageRequest, total);
        } catch (PersistenceException ex) {
            throw new InfrastructureException("Failed to execute project filter query", ex);
        }
    }

    @Override
    @Transactional
    public void save(Project project) {
        if (project == null) {
            throw new InfrastructureException("Project must not be null");
        }
        try {
            em.persist(mapper.toPersistenceModel(project));
            em.flush();
        } catch (PersistenceException ex) {
            if (ExceptionUtils.isConstraintViolation(ex)) {
                throw new AlreadyExistsException("Project with name or repoUrl already exists", ex);
            }
            throw new InfrastructureException("Failed to save new project: " + project.getName().value(), ex);
        }
    }

    @Override
    @Transactional
    public void update(Project project) {
        if (project == null) {
            throw new InfrastructureException("Project must not be null");
        }
        try {
            ProjectEntity existingProject = em.find(ProjectEntity.class, project.getId().value());
            if (existingProject == null) {
                throw new InfrastructureException("Cannot update non existing project with ID: " + project.getId().value());
            }
            em.merge(mapper.toPersistenceModel(project));
            em.flush();
        } catch (PersistenceException ex) {
            throw new InfrastructureException("Failed to update project with ID: " + project.getId().value(), ex);
        }
    }

    @Override
    @Transactional
    public void delete(ProjectId id) {
        if (id == null) {
            throw new InfrastructureException("Project id must not be null");
        }
        try {
            ProjectEntity project = em.find(ProjectEntity.class, id.value());
            if (project != null) {
                em.remove(project);
            }
        } catch (PersistenceException ex) {
            throw new InfrastructureException("Failed to delete project with ID: " + id.value(), ex);
        }
    }

    private long countSearchResults(CriteriaBuilder cb, RepoUrl repoUrl, Name name) {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ProjectEntity> root = cq.from(ProjectEntity.class);

        cq.select(cb.count(root)).where(buildPredicates(cb, root, repoUrl, name));

        return em.createQuery(cq).getSingleResult();
    }

    private List<ProjectEntity> fetchPageResults(CriteriaBuilder cb, RepoUrl repoUrl, Name name, PageRequest pageRequest) {
        CriteriaQuery<ProjectEntity> cq = cb.createQuery(ProjectEntity.class);
        Root<ProjectEntity> root = cq.from(ProjectEntity.class);

        cq.select(root).where(buildPredicates(cb, root, repoUrl, name));

        return em.createQuery(cq)
                .setFirstResult(pageRequest.offset())
                .setMaxResults(pageRequest.pageSize())
                .getResultList();
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<ProjectEntity> root, RepoUrl repoUrl, Name name) {
        List<Predicate> predicates = new ArrayList<>();

        if (repoUrl != null) {
            String urlPattern = "%" + repoUrl.toString().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get("repoUrl").as(String.class)), urlPattern));
        }

        if (name != null) {
            String namePattern = "%" + name.value().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get("name")), namePattern));
        }

        return predicates.toArray(Predicate[]::new);
    }
}
