package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.adapter;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.KPIAssignmentEntity;
import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper.KPIAssignmentJpaMapper;
import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.util.ExceptionUtils;
import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import de.thws.fiw.bs.kpi.application.domain.exception.InfrastructureException;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.out.KPIAssignmentRepository;
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
public class KPIAssignmentRepositoryAdapter implements KPIAssignmentRepository {

    @Inject
    KPIAssignmentJpaMapper mapper;

    @Inject
    EntityManager em;

    @Override
    public Optional<KPIAssignment> findById(KPIAssignmentId id) {
        try {
            KPIAssignmentEntity kpiAssignment = em.find(KPIAssignmentEntity.class, id.value());
            return Optional.ofNullable(kpiAssignment).map(mapper::toDomainModel);
        } catch (PersistenceException pe) {
            throw new InfrastructureException("Database access failed for ID: " + id.value(), pe);
        }
    }

    @Override
    public Page<KPIAssignment> findByFilter(KPIId kpiId, ProjectId projectId, PageRequest pageRequest) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            long total = countSearchResults(cb, kpiId, projectId);

            if (total == 0) {
                return new Page<>(List.of(), pageRequest, 0);
            }

            List<KPIAssignmentEntity> entities = fetchPageResults(cb, kpiId, projectId, pageRequest);
            return new Page<>(mapper.toDomainModels(entities), pageRequest, total);
        } catch (PersistenceException pe) {
            throw new InfrastructureException("Failed to execute kpiAssignment filter query", pe);
        }
    }

    @Override
    @Transactional
    public void save(KPIAssignment kpiAssignment) {
        try {
            em.persist(mapper.toPersistenceModel(kpiAssignment));
            em.flush();
        } catch (PersistenceException ex) {
            if (ExceptionUtils.isConstraintViolation(ex)) {
                throw new AlreadyExistsException("KPIAssignment with kpiId and projectId already exists", ex);//stimmt das so?
            }
            throw new InfrastructureException("Failed to save new kpiAssignment", ex);
        }
    }

    @Override
    @Transactional
    public void update(KPIAssignment kpiAssignment) {
        try {
            em.merge(mapper.toPersistenceModel(kpiAssignment));
            em.flush();
        } catch (PersistenceException ex) {
            throw new InfrastructureException("Failed to update kpiAssignment with ID: " + kpiAssignment.getId().value(), ex);
        }
    }

    @Override
    @Transactional
    public void delete(KPIAssignmentId id) {
        try {
            KPIAssignmentEntity kpiAssignment = em.find(KPIAssignmentEntity.class, id.value());
            if (kpiAssignment != null) {
                em.remove(kpiAssignment);
            }
        } catch (PersistenceException ex) {
            throw new InfrastructureException("Failed to delete kpiAssignment with ID: " + id.value(), ex);
        }
    }

    private long countSearchResults(CriteriaBuilder cb, KPIId kpiId, ProjectId projectId) {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<KPIAssignmentEntity> root = cq.from(KPIAssignmentEntity.class);

        cq.select(cb.count(root)).where(buildPredicates(cb, root, kpiId, projectId));

        return em.createQuery(cq).getSingleResult();
    }

    private List<KPIAssignmentEntity> fetchPageResults(CriteriaBuilder cb, KPIId kpiId, ProjectId projectId, PageRequest pageRequest) {
        CriteriaQuery<KPIAssignmentEntity> cq = cb.createQuery(KPIAssignmentEntity.class);
        Root<KPIAssignmentEntity> root = cq.from(KPIAssignmentEntity.class);

        cq.select(root).where(buildPredicates(cb, root, kpiId, projectId));

        return em.createQuery(cq)
                .setFirstResult(pageRequest.offset())
                .setMaxResults(pageRequest.pageSize())
                .getResultList();
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<KPIAssignmentEntity> root, KPIId kpiId, ProjectId projectId) {
        List<Predicate> predicates = new ArrayList<>();

        if (kpiId != null) {
            predicates.add(cb.equal(root.get("kpiEntity").get("id"), kpiId.value()));
        }

        if (projectId != null) {
            predicates.add(cb.equal(root.get("projectId"), projectId.value()));
        }

        return predicates.toArray(Predicate[]::new);
    }
}
