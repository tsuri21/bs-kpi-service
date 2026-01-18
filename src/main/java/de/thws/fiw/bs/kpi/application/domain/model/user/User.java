package de.thws.fiw.bs.kpi.application.domain.model.user;

import java.util.Objects;

public class User {
    private final UserId id;
    private final Username username;
    private final String passwordHash;
    private final Role role;

    public User(UserId id, Username username, String passwordHash, Role role) {
        this.id = Objects.requireNonNull(id, "User id must not be null");
        this.username = Objects.requireNonNull(username, "Username must not be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash must not be null");
        this.role = Objects.requireNonNull(role, "Role must not be null");
    }

    public UserId getId() {
        return id;
    }

    public Username getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }
}
