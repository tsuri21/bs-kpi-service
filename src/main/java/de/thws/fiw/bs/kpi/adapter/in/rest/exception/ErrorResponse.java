package de.thws.fiw.bs.kpi.adapter.in.rest.exception;

public record ErrorResponse(String error, String message, String path) {
}