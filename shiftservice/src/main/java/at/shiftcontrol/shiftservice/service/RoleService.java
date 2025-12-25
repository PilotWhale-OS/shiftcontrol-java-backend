package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.shiftservice.dto.roles.RoleAssignmentAssignDto;
import at.shiftcontrol.shiftservice.dto.roles.RoleAssignmentDto;
import at.shiftcontrol.shiftservice.dto.roles.RoleDto;
import at.shiftcontrol.shiftservice.dto.roles.RoleModificationDto;

public interface RoleService {
    Collection<RoleDto> getRoles(Long eventId);

    RoleDto getRole(Long eventId, Long roleId) throws ForbiddenException;

    RoleDto createRole(Long eventId, RoleModificationDto roleDto);

    RoleDto updateRole(Long eventId, Long roleId, RoleModificationDto roleDto) throws ForbiddenException;

    void deleteRole(Long eventId, Long roleId) throws ForbiddenException;

    Collection<RoleAssignmentDto> getRoleAssignmentsForUser(Long eventId, String userId);

    RoleAssignmentDto createRoleAssignment(Long eventId, String userId, RoleAssignmentAssignDto assignDto) throws ForbiddenException;

    void deleteRoleAssignment(Long eventId, String userId, Long roleAssignmentId) throws ForbiddenException;
}
