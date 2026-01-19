package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
import de.thws.fiw.bs.kpi.application.domain.model.user.User;
import de.thws.fiw.bs.kpi.application.domain.model.user.UserId;
import de.thws.fiw.bs.kpi.application.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserManagementService userManagementService;

    @Test
    void delete_userExists_callsRepository() {
        UserId id = UserId.newId();
        User mockUser = mock(User.class);

        when(userRepository.findById(id)).thenReturn(Optional.of(mockUser));

        userManagementService.delete(id);

        verify(userRepository).delete(id);
    }

    @Test
    void delete_userDoesNotExist_throwsException() {
        UserId id = UserId.newId();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userManagementService.delete(id));

        verify(userRepository, never()).delete(any());
    }
}