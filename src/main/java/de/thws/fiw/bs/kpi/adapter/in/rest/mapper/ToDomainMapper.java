package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import java.util.List;

public interface ToDomainMapper<D, R> {
    D toDomainModel(R dto);

    default List<D> toDomainModels(List<R> dtos) {
        return dtos == null ? List.of() : dtos.stream()
                .map(this::toDomainModel)
                .toList();
    }
}
