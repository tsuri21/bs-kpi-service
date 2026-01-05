package de.thws.fiw.bs.kpi.application.domain.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Object id) {
        super("Resource " + resourceName + " with ID " + id + " not found");
    }

    public ResourceNotFoundException(String resourceName, Object id, Throwable cause) {
        super("Resource " + resourceName + " with ID " + id + " not found", cause);
    }
}
