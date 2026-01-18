package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import de.thws.fiw.bs.kpi.application.domain.exception.AuthenticationException;
import de.thws.fiw.bs.kpi.application.domain.model.user.Role;
import de.thws.fiw.bs.kpi.application.domain.model.user.User;
import de.thws.fiw.bs.kpi.application.domain.model.user.UserId;
import de.thws.fiw.bs.kpi.application.domain.model.user.Username;
import de.thws.fiw.bs.kpi.application.port.in.AuthenticationUseCase;
import de.thws.fiw.bs.kpi.application.port.out.PasswordHasherPort;
import de.thws.fiw.bs.kpi.application.port.out.TokenProviderPort;
import de.thws.fiw.bs.kpi.application.port.out.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthenticationService implements AuthenticationUseCase {

    @Inject
    UserRepository userRepository;

    @Inject
    TokenProviderPort tokenProvider;

    @Inject
    PasswordHasherPort passwordHasher;

    @Override
    public String login(Username username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        boolean isCorrectPassword = passwordHasher.verify(password, user.getPasswordHash());

        if (!isCorrectPassword) {
            throw new AuthenticationException("Invalid credentials");
        }

        return tokenProvider.createAccessToken(user);
    }

    @Override
    public UserId register(Username username, String password, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new AlreadyExistsException("Username " + username.value() + " is already taken");
        }

        String passwordHash = passwordHasher.hash(password);

        UserId id = UserId.newId();
        User newUser = new User(id, username, passwordHash, role);
        userRepository.save(newUser);
        return id;
    }
}
