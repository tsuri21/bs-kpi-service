package de.thws.fiw.bs.kpi.application.domain.model.user;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void init_anyArgumentNull_throwsException() {
        UserId id = UserId.newId();
        Username username = new Username("Test user");
        String passwordHash = "abcHash";

        assertThrows(NullPointerException.class, () -> new User(null, username, passwordHash, Role.ADMIN));
        assertThrows(NullPointerException.class, () -> new User(id, null, passwordHash, Role.ADMIN));
        assertThrows(NullPointerException.class, () -> new User(id, username, null, Role.ADMIN));
        assertThrows(NullPointerException.class, () -> new User(id, username, passwordHash, null));
    }
}