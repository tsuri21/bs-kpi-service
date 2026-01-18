package de.thws.fiw.bs.kpi.application.domain.service;

import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
import de.thws.fiw.bs.kpi.application.domain.model.user.User;
import de.thws.fiw.bs.kpi.application.domain.model.user.UserId;
import de.thws.fiw.bs.kpi.application.port.in.UserManagementUseCase;
import de.thws.fiw.bs.kpi.application.port.out.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class UserManagementService implements UserManagementUseCase {

    @Inject
    UserRepository userRepository;

    @Override
    public Optional<User> readById(UserId id) {
        return userRepository.findById(id);
    }

    @Override
    public void delete(UserId id) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        userRepository.delete(id);
    }
}
