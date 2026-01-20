package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import java.util.List;

public interface ToApiMapper<D, R> {
    R toApiModel(D domain);

    default List<R> toApiModels(List<D> domains) {
        return domains == null ? List.of() : domains.stream()
                .map(this::toApiModel)
                .toList();
    }
}
