package de.thws.fiw.bs.kpi.adapter.out.security;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class BcryptAdapterTest {

    @Inject
    BcryptAdapter adapter;

    @Test
    void hashAndVerify_validPassword_worksCorrectly() {
        String rawPassword = "mySuperSecretPassword123";

        String hash = adapter.hash(rawPassword);

        assertNotEquals(rawPassword, hash);
        assertTrue(hash.startsWith("$2a$"));

        assertTrue(adapter.verify(rawPassword, hash));
        assertFalse(adapter.verify("wrongPassword", hash));
    }
}