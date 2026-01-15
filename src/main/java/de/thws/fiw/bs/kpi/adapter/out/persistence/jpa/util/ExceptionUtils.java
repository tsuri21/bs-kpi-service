package de.thws.fiw.bs.kpi.adapter.out.persistence.jpa.util;

import org.hibernate.exception.ConstraintViolationException;

import java.sql.SQLException;

public class ExceptionUtils {

    private static final String SQL_STATE_UNIQUE_VIOLATION = "23505";

    private ExceptionUtils() {}

    public static boolean isUniqueConstraintViolation(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve) {
                if (SQL_STATE_UNIQUE_VIOLATION.equals(cve.getSQLState())) {
                    return true;
                }
            }

            if (cause instanceof SQLException sqlEx) {
                if (SQL_STATE_UNIQUE_VIOLATION.equals(sqlEx.getSQLState())) {
                    return true;
                }
            }

            cause = cause.getCause();
        }
        return false;
    }
}
