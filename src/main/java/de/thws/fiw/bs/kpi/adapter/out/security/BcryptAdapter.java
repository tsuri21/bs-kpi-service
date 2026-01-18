package de.thws.fiw.bs.kpi.adapter.out.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import de.thws.fiw.bs.kpi.application.port.out.PasswordHasherPort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BcryptAdapter implements PasswordHasherPort {

    @Override
    public String hash(String raw) {
        return BCrypt.withDefaults().hashToString(10, raw.toCharArray());
    }

    @Override
    public boolean verify(String rawPassword, String encodedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword);
        return result.verified;
    }
}
