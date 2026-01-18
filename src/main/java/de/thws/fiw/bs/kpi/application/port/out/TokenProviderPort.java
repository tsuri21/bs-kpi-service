package de.thws.fiw.bs.kpi.application.port.out;

import de.thws.fiw.bs.kpi.application.domain.model.user.User;

public interface TokenProviderPort {
    String createAccessToken(User user);
}
