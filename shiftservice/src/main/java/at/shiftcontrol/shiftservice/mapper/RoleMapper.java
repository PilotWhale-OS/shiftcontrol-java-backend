package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dto.role.RoleDto;
import at.shiftcontrol.shiftservice.dto.role.RoleModificationDto;
import at.shiftcontrol.shiftservice.entity.role.Role;

@RequiredArgsConstructor
@Service
public class RoleMapper {
    public static RoleDto toRoleDto(Role role) {
        return new RoleDto(
            String.valueOf(role.getId()),
            role.getName(),
            role.getDescription(),
            role.isSelfAssignable()
        );
    }

    public static Collection<RoleDto> toRoleDto(Collection<Role> roles) {
        return roles.stream()
            .map(RoleMapper::toRoleDto)
            .toList();
    }

    public static Role toRole(RoleModificationDto roleDto) {
        return Role.builder()
            .name(roleDto.getName())
            .description(roleDto.getDescription())
            .selfAssignable(roleDto.isSelfAssignable())
            .build();
    }

    public static void updateRole(RoleModificationDto roleDto, Role role) {
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());
        role.setSelfAssignable(roleDto.isSelfAssignable());
    }
}
