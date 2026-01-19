package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import de.thws.fiw.bs.kpi.application.domain.exception.AuthenticationException;
import de.thws.fiw.bs.kpi.application.domain.model.user.Role;
import de.thws.fiw.bs.kpi.application.domain.model.user.User;
import de.thws.fiw.bs.kpi.application.domain.model.user.UserId;
import de.thws.fiw.bs.kpi.application.domain.model.user.Username;
import de.thws.fiw.bs.kpi.application.port.out.PasswordHasherPort;
import de.thws.fiw.bs.kpi.application.port.out.TokenProviderPort;
import de.thws.fiw.bs.kpi.application.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    TokenProviderPort tokenProvider;

    @Mock
    PasswordHasherPort passwordHasher;

    @InjectMocks
    AuthenticationService authService;

    @Test
    void login_validCredentials_returnsToken() {
        Username username = new Username("Alice");
        String rawPassword = "password123";
        String storedHash = "hashed_secret";

        User foundUser = new User(UserId.newId(), username, storedHash, Role.MEMBER);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(foundUser));
        when(passwordHasher.verify(rawPassword, storedHash)).thenReturn(true);
        when(tokenProvider.createAccessToken(foundUser)).thenReturn("valid.jwt.token");

        String token = authService.login(username, rawPassword);

        assertEquals("valid.jwt.token", token);
        verify(tokenProvider).createAccessToken(foundUser);
    }

    @Test
    void login_userNotFound_throwsException() {
        Username username = new Username("Ghost");
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () ->
                authService.login(username, "anyPassword")
        );

        verifyNoInteractions(passwordHasher);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void login_wrongPassword_throwsException() {
        Username username = new Username("Alice");
        String wrongPassword = "wrong";
        String storedHash = "correct_hash";

        User foundUser = new User(UserId.newId(), username, storedHash, Role.MEMBER);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(foundUser));
        when(passwordHasher.verify(wrongPassword, storedHash)).thenReturn(false);

        assertThrows(AuthenticationException.class, () ->
                authService.login(username, wrongPassword)
        );

        verifyNoInteractions(tokenProvider);
    }

    @Test
    void register_newUser_savesUserAndReturnsId() {
        Username username = new Username("NewUser");
        String password = "securePW";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordHasher.hash(password)).thenReturn("hashed_new_pw");

        UserId resultId = authService.register(username, password, Role.ADMIN);

        assertNotNull(resultId);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();

        assertEquals(username, capturedUser.getUsername());
        assertEquals("hashed_new_pw", capturedUser.getPasswordHash());
        assertEquals(Role.ADMIN, capturedUser.getRole());
    }

    @Test
    void register_userAlreadyExists_throwsException() {
        Username username = new Username("ExistingUser");
        User existingUser = mock(User.class);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        assertThrows(AlreadyExistsException.class, () ->
                authService.register(username, "pw", Role.MEMBER)
        );

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordHasher);
    }
}