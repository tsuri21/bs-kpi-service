package de.thws.fiw.bs.kpi.application.port;

import java.util.List;

public record Page<T>(List<T> content, PageRequest pageRequest, long totalElements) {
    public Page {
        if (totalElements < 0) {
            throw new IllegalArgumentException("Total must not be negative");
        }
    }

    public long getTotalPages() {
        return (long) Math.ceil((double) totalElements / pageRequest.pageSize());
    }

    public boolean hasNext() {
        return (long) pageRequest.pageNumber() * pageRequest.pageSize() < totalElements;
    }

    public boolean hasPrevious() {
        return pageRequest.pageNumber() > 1;
    }
}
