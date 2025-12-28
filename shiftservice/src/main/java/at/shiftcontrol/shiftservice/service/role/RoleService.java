package at.shiftcontrol.shiftservice.service.role;

import java.util.Collection;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.shiftservice.dto.role.RoleDto;
import at.shiftcontrol.shiftservice.dto.role.RoleModificationDto;
import at.shiftcontrol.shiftservice.dto.role.UserRoleAssignmentAssignDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;

public interface RoleService {
    Collection<RoleDto> getRoles(Long shiftPlanId);

    RoleDto getRole(Long shiftPlanId, Long roleId) throws ForbiddenException;

    RoleDto createRole(Long shiftPlanId, RoleModificationDto roleDto) throws ForbiddenException;

    RoleDto updateRole(Long shiftPlanId, Long roleId, RoleModificationDto roleDto) throws ForbiddenException;

    void deleteRole(Long shiftPlanId, Long roleId) throws ForbiddenException;

    VolunteerDto createUserRoleAssignment(Long shiftPlanId, String userId, UserRoleAssignmentAssignDto assignDto) throws ForbiddenException;

    void deleteUserRoleAssignment(Long shiftPlanId, String userId, Long roleId) throws ForbiddenException;
}
