package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.Collections;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dto.role.RoleDto;
import at.shiftcontrol.shiftservice.dto.role.RoleModificationDto;
import at.shiftcontrol.shiftservice.entity.role.Role;

@RequiredArgsConstructor
@Service
public class RoleMapper {
    public static RoleDto toRoleDto(@NonNull Role role) {
        return new RoleDto(
            String.valueOf(role.getId()),
            role.getName(),
            role.getDescription(),
            role.isSelfAssignable()
        );
    }

    public static Collection<RoleDto> toRoleDto(Collection<Role> roles) {
        if (roles == null) {
            return Collections.emptyList();
        }
        return roles.stream()
            .map(RoleMapper::toRoleDto)
            .toList();
    }

    public static Role toRole(@NonNull RoleModificationDto roleDto) {
        return Role.builder()
            .name(roleDto.getName())
            .description(roleDto.getDescription())
            .selfAssignable(roleDto.isSelfAssignable())
            .build();
    }
}
