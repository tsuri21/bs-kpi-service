package de.thws.fiw.bs.kpi.application.port.out;

public interface PasswordHasherPort {
    String hash(String rawPassword);

    boolean verify(String rawPassword, String encodedPassword);
}
