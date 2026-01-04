package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.adapter;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.KPIEntity;
import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper.KPIMapper;
import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.util.ExceptionUtils;
import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import de.thws.fiw.bs.kpi.application.domain.exception.InfrastructureException;
import de.thws.fiw.bs.kpi.application.domain.model.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.out.KPIRepository;
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
public class KPIRepositoryAdapter implements KPIRepository {

    @Inject
    KPIMapper mapper;

    @Inject
    EntityManager em;

    @Override
    public Optional<KPI> findById(KPIId id) {
        try {
            KPIEntity kpi = em.find(KPIEntity.class, id.value());
            return Optional.ofNullable(mapper.toDomainModel(kpi));
        } catch (PersistenceException ex) {
            throw new InfrastructureException("Database access failed for ID: " + id.value(), ex);
        }
    }

    @Override
    public Page<KPI> findByFilter(Name name, PageRequest pageRequest) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            long total = countSearchResults(cb, name);

            if (total == 0) {
                return new Page<>(List.of(), pageRequest, 0);
            }

            List<KPIEntity> entities = fetchPageResults(cb, name, pageRequest);
            return new Page<>(mapper.toDomainModels(entities), pageRequest, total);
        } catch (PersistenceException ex) {
            throw new InfrastructureException("Failed to execute KPI filter query", ex);
        }
    }

    @Override
    @Transactional
    public void save(KPI kpi) {
        if (kpi == null) {
            throw new InfrastructureException("KPI must not be null");
        }
        try {
            em.persist(mapper.toPersistenceModel(kpi));
            em.flush();
        } catch (PersistenceException ex) {
            if (ExceptionUtils.isConstraintViolation(ex)) {
                throw new AlreadyExistsException("KPI with name already exists", ex);
            }
            throw new InfrastructureException("Failed to save KPI: " + kpi.getName().value(), ex);
        }
    }

    @Override
    @Transactional
    public void update(KPI kpi) {
        if (kpi == null) {
            throw new InfrastructureException("KPI must not be null");
        }
        try {
            KPIEntity exsitingKPI = em.find(KPIEntity.class, kpi.getId().value());
            if (exsitingKPI == null) {
                throw new InfrastructureException("Cannot update non existing KPI with ID: " + kpi.getId().value());
            }
            em.merge(mapper.toPersistenceModel(kpi));
            em.flush();
        } catch (PersistenceException ex) {
            throw new InfrastructureException("Failed to update KPI with ID: " + kpi.getId().value(), ex);
        }
    }

    @Override
    @Transactional
    public void delete(KPIId id) {
        if (id == null) {
            throw new InfrastructureException("KPI must not be null");
        }
        try {
            KPIEntity kpi = em.find(KPIEntity.class, id.value());
            if (kpi != null) {
                em.remove(kpi);
            }
        } catch (PersistenceException ex) {
            throw new InfrastructureException("Failed to delete KPI: " + id.value(), ex);
        }
    }

    private long countSearchResults(CriteriaBuilder cb, Name name) {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<KPIEntity> root = cq.from(KPIEntity.class);

        cq.select(cb.count(root)).where(buildPredicates(cb, root, name));

        return em.createQuery(cq).getSingleResult();
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<KPIEntity> root, Name name) {
        List<Predicate> predicates = new ArrayList<>();
        if (name != null) {
            String namePattern = "%" + name.value().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get("name")), namePattern));
        }
        return predicates.toArray(Predicate[]::new);
    }

    private List<KPIEntity> fetchPageResults(CriteriaBuilder cb, Name name, PageRequest pageRequest) {
        CriteriaQuery<KPIEntity> cq = cb.createQuery(KPIEntity.class);
        Root<KPIEntity> root = cq.from(KPIEntity.class);

        cq.select(root).where(buildPredicates(cb, root, name));

        return em.createQuery(cq)
                .setFirstResult(pageRequest.offset())
                .setMaxResults(pageRequest.pageSize())
                .getResultList();
    }
}
