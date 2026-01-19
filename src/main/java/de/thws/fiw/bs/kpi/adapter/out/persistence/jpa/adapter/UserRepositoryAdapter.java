package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.adapter;

import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.entity.UserEntity;
import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper.UserJpaMapper;
import de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.util.ExceptionUtils;
import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import de.thws.fiw.bs.kpi.application.domain.exception.InfrastructureException;
import de.thws.fiw.bs.kpi.application.domain.model.user.User;
import de.thws.fiw.bs.kpi.application.domain.model.user.UserId;
import de.thws.fiw.bs.kpi.application.domain.model.user.Username;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.out.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepositoryAdapter implements UserRepository {

    @Inject
    EntityManager em;

    @Inject
    UserJpaMapper mapper;

    @Override
    public Optional<User> findById(UserId id) {
        try {
            UserEntity user = em.find(UserEntity.class, id.value());
            return Optional.ofNullable(user).map(mapper::toDomainModel);
        } catch (PersistenceException ex) {
            throw new InfrastructureException("Database access failed for user with ID: " + id.value(), ex);
        }
    }

    @Override
    public Optional<User> findByUsername(Username name) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<UserEntity> cq = cb.createQuery(UserEntity.class);
            Root<UserEntity> root = cq.from(UserEntity.class);

            cq.select(root).where(cb.equal(root.get("username"), name.value()));

            return em.createQuery(cq)
                    .getResultStream()
                    .findFirst()
                    .map(mapper::toDomainModel);

        } catch (PersistenceException ex) {
            throw new InfrastructureException("Failed to find user by username: " + name.value(), ex);
        }
    }

    @Override
    public Page<User> findAll(PageRequest pageRequest) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();

            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<UserEntity> countRoot = countQuery.from(UserEntity.class);
            countQuery.select(cb.count(countRoot));
            Long total = em.createQuery(countQuery).getSingleResult();

            if (total == 0) {
                return new Page<>(List.of(), pageRequest, 0);
            }

            CriteriaQuery<UserEntity> selectQuery = cb.createQuery(UserEntity.class);
            Root<UserEntity> selectRoot = selectQuery.from(UserEntity.class);
            selectQuery.select(selectRoot);

            List<UserEntity> entities = em.createQuery(selectQuery)
                    .setFirstResult(pageRequest.offset())
                    .setMaxResults(pageRequest.pageSize())
                    .getResultList();

            return new Page<>(mapper.toDomainModels(entities), pageRequest, total);

        } catch (PersistenceException ex) {
            throw new InfrastructureException("Failed to load users", ex);
        }
    }


    @Override
    @Transactional
    public void save(User user) {
        try {
            UserEntity entity = mapper.toPersistenceModel(user);
            em.persist(entity);
            em.flush();
        } catch (PersistenceException ex) {
            if (ExceptionUtils.isUniqueConstraintViolation(ex)) {
                throw new AlreadyExistsException("User with username " + user.getUsername() + " already exists");
            }
            throw new InfrastructureException("Failed to save new user: " + user.getUsername().value(), ex);
        }
    }

    @Override
    @Transactional
    public void delete(UserId id) {
        try {
            UserEntity entity = em.find(UserEntity.class, id.value());
            if (entity != null) {
                em.remove(entity);
            }
        } catch (PersistenceException ex) {
            throw new InfrastructureException("Failed to delete user with ID: " + id.value(), ex);
        }
    }
}
