package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.mapper;

import java.util.List;

public interface PersistenceMapper<D, E> {
    D toDomainModel(E entity);

    E toPersistenceModel(D domain);

    default List<D> toDomainModels(List<E> entities) {
        return entities == null ? List.of() : entities.stream()
                .map(this::toDomainModel)
                .toList();
    }

    default List<E> toPersistenceModels(List<D> domains) {
        return domains == null ? List.of() : domains.stream()
                .map(this::toPersistenceModel)
                .toList();
    }
}