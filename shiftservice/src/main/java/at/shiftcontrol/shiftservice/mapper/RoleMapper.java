package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dto.role.RoleAssignmentDto;
import at.shiftcontrol.shiftservice.dto.role.RoleDto;
import at.shiftcontrol.shiftservice.dto.role.RoleModificationDto;
import at.shiftcontrol.shiftservice.entity.role.Role;
import at.shiftcontrol.shiftservice.entity.role.RoleAssignment;

@RequiredArgsConstructor
@Service
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

    public static Role toRole(RoleModificationDto roleDto) {
        return Role.builder()
            .name(roleDto.getName())
            .description(roleDto.getDescription())
            .build();
    }

    public static Collection<RoleAssignmentDto> toRoleAssignmentDto(List<RoleAssignment> roleAssignments) {
        return roleAssignments.stream()
            .map(RoleMapper::toRoleAssignmentDto)
            .collect(Collectors.toList());
    }

    public static RoleAssignmentDto toRoleAssignmentDto(RoleAssignment roleAssignment) {
        return RoleAssignmentDto.builder()
            .id(roleAssignment.getId())
            .userId(roleAssignment.getUserId())
            .roleId(roleAssignment.getRole().getId())
            .build();
    }
}
