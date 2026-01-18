package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.user.User;
import de.thws.fiw.bs.kpi.application.domain.model.user.UserId;

import java.util.Optional;

public interface UserManagementUseCase {

    Optional<User> readById(UserId id);

    void delete(UserId id);
}