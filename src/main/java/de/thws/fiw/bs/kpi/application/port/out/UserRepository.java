package de.thws.fiw.bs.kpi.application.port.out;

import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.user.User;
import de.thws.fiw.bs.kpi.application.domain.model.user.UserId;
import de.thws.fiw.bs.kpi.application.domain.model.user.Username;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(UserId id);

    Optional<User> findByUsername(Username name);

    void save(User user);

    void delete(UserId id);
}
