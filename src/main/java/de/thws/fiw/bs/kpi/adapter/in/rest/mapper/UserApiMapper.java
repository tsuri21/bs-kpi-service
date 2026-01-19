package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.user.UserResponseDTO;
import de.thws.fiw.bs.kpi.application.domain.model.user.User;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class UserApiMapper {

    @Inject
    RoleApiMapper roleApiMapper;

    public UserResponseDTO toApiModel(User user) {
        return new UserResponseDTO(
                user.getId().value(),
                user.getUsername().value(),
                roleApiMapper.toApiModel(user.getRole())
        );
    }

    public List<UserResponseDTO> toApiModels(List<User> users) {
        return users == null ? List.of() : users.stream()
                .map(this::toApiModel)
                .toList();
    }
}
