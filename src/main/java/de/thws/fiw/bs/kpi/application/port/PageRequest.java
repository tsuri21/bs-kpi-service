package de.thws.fiw.bs.kpi.application.port;

public record PageRequest(int pageNumber, int pageSize) {
    public PageRequest {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
    }

    public int offset() {
        return (pageNumber - 1) * pageSize;
    }
}
