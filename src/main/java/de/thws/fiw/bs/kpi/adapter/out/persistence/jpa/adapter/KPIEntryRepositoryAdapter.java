package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.adapter;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.KPIEntryEntity;
import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper.KPIEntryJpaMapper;
import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.util.ExceptionUtils;
import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import de.thws.fiw.bs.kpi.application.domain.exception.InfrastructureException;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntryId;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.out.KPIEntryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class KPIEntryRepositoryAdapter implements KPIEntryRepository {

    @Inject
    KPIEntryJpaMapper mapper;

    @Inject
    EntityManager em;

    @Override
    public Optional<KPIEntry> findById(KPIEntryId id){
        try{
            KPIEntryEntity kpiEntry = em.find(KPIEntryEntity.class, id.value());
            return Optional.ofNullable(kpiEntry).map(mapper::toDomainModel);
        } catch (PersistenceException ex){
            throw new InfrastructureException("Database access failed for ID: " + id.value(), ex);
        }
    }

    @Override
    public Optional<KPIEntry> findLatest(KPIAssignmentId id){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<KPIEntryEntity> cq = cb.createQuery(KPIEntryEntity.class);
        Root<KPIEntryEntity> root = cq.from(KPIEntryEntity.class);

        cq.where(cb.equal(root.get("assignmentId"), id.value()));

        cq.orderBy(cb.desc(root.get("timestamp")));

        try{
            List<KPIEntryEntity> results = em.createQuery(cq)
                    .setMaxResults(1)
                    .getResultList();

            return results.stream().map(mapper::toDomainModel).findFirst();
        }
        catch (PersistenceException ex){
            throw new InfrastructureException("Database access failed for AssignmentID: " + id.value(), ex);
        }
    }

    @Override
    public Page<KPIEntry> findByFilter(KPIAssignmentId id, Instant from, Instant to, PageRequest pageRequest){
        try{
            CriteriaBuilder cb = em.getCriteriaBuilder();
            long total = countSearchResults(cb, id, from, to);

            if(total == 0){
                return new Page<>(List.of(), pageRequest, 0);
            }

            List<KPIEntryEntity> entities = fetchPageResults(cb, id, from, to, pageRequest);
            return new Page<>(mapper.toDomainModels(entities), pageRequest, total);
        } catch (PersistenceException ex){
            throw new InfrastructureException("Failed to execute KPIEntry filter query", ex);
        }
    }

    @Override
    @Transactional
    public void save(KPIEntry kpiEntry){
        try{
            em.persist(mapper.toPersistenceModel(kpiEntry));
            em.flush();
        } catch (PersistenceException ex){
            if(ExceptionUtils.isUniqueConstraintViolation(ex)){
                throw new AlreadyExistsException("KPIEntry with kpiAssignmentId and timestamp already exists", ex);
            }
            throw new InfrastructureException("Failed to save new kpiEntry", ex);
        }
    }

    @Override
    @Transactional
    public void delete(KPIEntryId id){
        try{
            KPIEntryEntity kpiEntry = em.find(KPIEntryEntity.class, id.value());
            if(kpiEntry != null){
                em.remove(kpiEntry);
            }
        } catch (PersistenceException ex){
            throw new InfrastructureException("Failed to delete kpiEntry with ID: " + id.value(), ex);
        }
    }

    private long countSearchResults(CriteriaBuilder cb, KPIAssignmentId id, Instant from, Instant to) {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<KPIEntryEntity> root = cq.from(KPIEntryEntity.class);

        cq.select(cb.count(root)).where(buildPredicates(cb, root, id, from, to));

        return em.createQuery(cq).getSingleResult();
    }

    private List<KPIEntryEntity> fetchPageResults(CriteriaBuilder cb, KPIAssignmentId id, Instant from, Instant to, PageRequest pageRequest) {
        CriteriaQuery<KPIEntryEntity> cq = cb.createQuery(KPIEntryEntity.class);
        Root<KPIEntryEntity> root = cq.from(KPIEntryEntity.class);

        cq.select(root).where(buildPredicates(cb, root, id, from, to));

        return em.createQuery(cq)
                .setFirstResult(pageRequest.offset())
                .setMaxResults(pageRequest.pageSize())
                .getResultList();
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<KPIEntryEntity> root, KPIAssignmentId id, Instant from, Instant to) {
        List<Predicate> predicates = new ArrayList<>();

        if (id != null) {
            predicates.add(cb.equal(root.get("assignmentId"), id.value()));
        }

        if (from != null && to != null) {
            predicates.add(cb.between(root.get("timestamp"), from, to));
        } else {
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), from));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), to));
            }
        }

        return predicates.toArray(Predicate[]::new);
    }

}
