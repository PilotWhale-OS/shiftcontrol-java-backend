package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.shiftservice.dto.roles.RoleAssignmentAssignDto;
import at.shiftcontrol.shiftservice.dto.roles.RoleAssignmentDto;
import at.shiftcontrol.shiftservice.dto.roles.RoleDto;
import at.shiftcontrol.shiftservice.dto.roles.RoleModificationDto;

public interface RoleService {
    Collection<RoleDto> getRoles(Long shiftPlanId);

    RoleDto getRole(Long shiftPlanId, Long roleId) throws ForbiddenException;

    RoleDto createRole(Long shiftPlanId, RoleModificationDto roleDto);

    RoleDto updateRole(Long shiftPlanId, Long roleId, RoleModificationDto roleDto) throws ForbiddenException;

    void deleteRole(Long shiftPlanId, Long roleId) throws ForbiddenException;

    Collection<RoleAssignmentDto> getRoleAssignmentsForUser(Long shiftPlanId, String userId);

    RoleAssignmentDto createRoleAssignment(Long shiftPlanId, String userId, RoleAssignmentAssignDto assignDto) throws ForbiddenException;

    void deleteRoleAssignment(Long shiftPlanId, String userId, Long roleAssignmentId) throws ForbiddenException;
}
