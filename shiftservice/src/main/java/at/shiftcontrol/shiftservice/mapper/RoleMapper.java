package at.shiftcontrol.shiftservice.mapper;

import at.shiftcontrol.shiftservice.dto.RoleDto;
import at.shiftcontrol.shiftservice.entity.Role;

public class RoleMapper {
    public static RoleDto toRoleDto(Role role) {
        return new RoleDto(
            String.valueOf(role.getId()),
            role.getName(),
            role.getDescription());
    }
}
