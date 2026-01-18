package de.thws.fiw.bs.kpi.adapter.out.security;

import de.thws.fiw.bs.kpi.application.domain.model.user.User;
import de.thws.fiw.bs.kpi.application.port.out.TokenProviderPort;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;

@ApplicationScoped
public class JwtTokenAdapter implements TokenProviderPort {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @Override
    public String createAccessToken(User user) {
        return Jwt.issuer(issuer)
                .upn(user.getUsername().value())
                .subject(user.getId().value().toString())
                .groups(Set.of(user.getRole().name()))
                .expiresIn(3600)
                .sign();
    }
}