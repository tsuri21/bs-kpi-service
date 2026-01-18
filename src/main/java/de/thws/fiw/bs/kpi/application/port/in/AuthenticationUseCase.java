package de.thws.fiw.bs.kpi.application.port.in;

import de.thws.fiw.bs.kpi.application.domain.model.user.Role;
import de.thws.fiw.bs.kpi.application.domain.model.user.UserId;
import de.thws.fiw.bs.kpi.application.domain.model.user.Username;

public interface AuthenticationUseCase {

    String login(Username username, String password);

    UserId register(Username username, String password, Role role);
}
