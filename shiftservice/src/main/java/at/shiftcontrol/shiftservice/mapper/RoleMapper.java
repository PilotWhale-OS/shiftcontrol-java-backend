package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.RoleDto;
import at.shiftcontrol.shiftservice.entity.Role;

public class RoleMapper {
    public static RoleDto toRoleDto(Role role) {
        return new RoleDto(
            String.valueOf(role.getId()),
            role.getName(),
            role.getDescription());
    }

    public static Collection<RoleDto> toRoleDto(Collection<Role> roles) {
        return roles.stream()
            .map(RoleMapper::toRoleDto)
            .toList();
    }
}
