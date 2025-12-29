package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.util;

import org.hibernate.exception.ConstraintViolationException;

public class ExceptionUtils {
    private ExceptionUtils() {}

    public static boolean isConstraintViolation(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
