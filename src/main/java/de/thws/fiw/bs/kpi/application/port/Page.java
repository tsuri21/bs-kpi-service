package de.thws.fiw.bs.kpi.application.port;

import java.util.List;

public class Page<T> {
    private final List<T> content;
    private final PageRequest pageRequest;
    private final double total;

    public Page(List<T> content, PageRequest pageRequest, double total) {
        this.content = content;
        this.pageRequest = pageRequest;
        if (total <= 0) {
            throw new IllegalArgumentException("Total must be greater than zero");
        }
        this.total = total;
    }

    public List<T> getContent() {
        return content;
    }

    public PageRequest getPageRequest() {
        return pageRequest;
    }

    public double getTotal() {
        return total;
    }

    public double getTotalPages() {
        return Math.ceil(total / pageRequest.pageSize());
    }
}
