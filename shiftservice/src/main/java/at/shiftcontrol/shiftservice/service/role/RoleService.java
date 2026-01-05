package at.shiftcontrol.shiftservice.service.role;

import java.util.Collection;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.role.RoleDto;
import at.shiftcontrol.shiftservice.dto.role.RoleModificationDto;
import at.shiftcontrol.shiftservice.dto.role.UserRoleAssignmentAssignDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;

public interface RoleService {
    Collection<RoleDto> getRoles(Long shiftPlanId);

    RoleDto getRole(Long roleId) throws ForbiddenException, NotFoundException;

    RoleDto createRole(Long shiftPlanId, RoleModificationDto roleDto) throws ForbiddenException, NotFoundException;

    RoleDto updateRole(Long roleId, RoleModificationDto roleDto) throws ForbiddenException, NotFoundException;

    void deleteRole(Long roleId) throws ForbiddenException, NotFoundException;

    VolunteerDto createUserRoleAssignment(String userId, UserRoleAssignmentAssignDto assignDto) throws ForbiddenException, NotFoundException;

    void deleteUserRoleAssignment(String userId, Long roleId) throws ForbiddenException, NotFoundException;
}
