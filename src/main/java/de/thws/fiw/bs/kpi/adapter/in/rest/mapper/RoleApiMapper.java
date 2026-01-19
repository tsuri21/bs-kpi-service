package de.thws.fiw.bs.kpi.adapter.in.rest.mapper;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.user.RoleDTO;
import de.thws.fiw.bs.kpi.application.domain.model.user.Role;
import jakarta.inject.Singleton;

@Singleton
public class RoleApiMapper implements ApiMapper<Role, RoleDTO> {

    @Override
    public RoleDTO toApiModel(Role domain) {
        return switch (domain) {
            case ADMIN -> RoleDTO.ADMIN;
            case TECHNICAL_USER -> RoleDTO.TECHNICAL_USER;
            case MEMBER -> RoleDTO.MEMBER;
        };
    }

    @Override
    public Role toDomainModel(RoleDTO dto) {
        return switch (dto) {
            case ADMIN -> Role.ADMIN;
            case TECHNICAL_USER -> Role.TECHNICAL_USER;
            case MEMBER -> Role.MEMBER;
        };
    }
}
