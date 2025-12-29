package de.thws.fiw.bs.kpi.application.port;

import java.util.List;

public record Page<T>(List<T> content, PageRequest pageRequest, double totalElements) {
    public Page {
        if (totalElements < 0) {
            throw new IllegalArgumentException("Total must not be negative");
        }
    }

    public double getTotalPages() {
        return Math.ceil(totalElements / pageRequest.pageSize());
    }
}
