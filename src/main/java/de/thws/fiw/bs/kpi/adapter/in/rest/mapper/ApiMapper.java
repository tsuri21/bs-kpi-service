package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import java.util.List;

public interface ApiMapper<D, T> {
    T toApiModel(D domain);

    D toDomainModel(T dto);

    default List<T> toApiModels(List<D> domains) {
        return domains == null ? List.of() : domains.stream()
                .map(this::toApiModel)
                .toList();
    }

    default List<D> toDomainModels(List<T> dtos) {
        return dtos == null ? List.of() : dtos.stream()
                .map(this::toDomainModel)
                .toList();
    }
}
