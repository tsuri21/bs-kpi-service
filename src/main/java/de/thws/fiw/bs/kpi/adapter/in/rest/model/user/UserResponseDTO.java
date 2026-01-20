package de.thws.fiw.bs.kpi.adapter.in.rest.model.user;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.AbstractDTO;

import java.util.Objects;
import java.util.UUID;

public class UserResponseDTO extends AbstractDTO {

    private String username;
    private RoleDTO role;

    public UserResponseDTO() {
    }

    public UserResponseDTO(UUID id, String username, RoleDTO role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public RoleDTO getRole() {
        return role;
    }

    public void setRole(RoleDTO role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserResponseDTO that = (UserResponseDTO) o;
        return Objects.equals(username, that.username) && role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, role);
    }
}
