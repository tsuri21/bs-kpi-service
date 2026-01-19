package de.thws.fiw.bs.kpi.adapter.out.security;

import de.thws.fiw.bs.kpi.application.domain.model.user.Role;
import de.thws.fiw.bs.kpi.application.domain.model.user.User;
import de.thws.fiw.bs.kpi.application.domain.model.user.UserId;
import de.thws.fiw.bs.kpi.application.domain.model.user.Username;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class JwtTokenAdapterTest {

    @Inject
    JwtTokenAdapter adapter;

    @Inject
    JWTParser parser;

    @Test
    void createAccessToken_validData_containsCorrectClaims() throws ParseException {
        UserId id = UserId.newId();
        User user = new User(id, new Username("TestUser"), "hash", Role.ADMIN);

        String tokenString = adapter.createAccessToken(user);

        assertNotNull(tokenString);

        JsonWebToken token = parser.parse(tokenString);

        assertEquals("TestUser", token.getName());
        assertEquals(id.value().toString(), token.getSubject());
        assertTrue(token.getGroups().contains("ADMIN"));
        assertNotNull(token.getIssuer());
    }
}