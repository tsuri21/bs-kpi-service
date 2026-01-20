package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.user.UserResponseDTO;
import de.thws.fiw.bs.kpi.application.domain.model.user.User;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class UserApiMapper implements ToApiMapper<User, UserResponseDTO> {

    @Inject
    RoleApiMapper roleApiMapper;

    @Override
    public UserResponseDTO toApiModel(User user) {
        return new UserResponseDTO(
                user.getId().value(),
                user.getUsername().value(),
                roleApiMapper.toApiModel(user.getRole())
        );
    }
}
