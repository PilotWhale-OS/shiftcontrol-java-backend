package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.role.RoleDto;
import at.shiftcontrol.shiftservice.dto.role.RoleModificationDto;
import at.shiftcontrol.shiftservice.dto.role.UserRoleAssignmentAssignDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;

public interface RoleService {
    Collection<RoleDto> getRoles(Long shiftPlanId);

    RoleDto getRole(Long roleId);

    RoleDto createRole(Long shiftPlanId, RoleModificationDto roleDto);

    RoleDto updateRole(Long roleId, RoleModificationDto roleDto);

    void deleteRole(Long roleId);

    VolunteerDto createUserRoleAssignment(String userId, UserRoleAssignmentAssignDto assignDto);

    void deleteUserRoleAssignment(String userId, Long roleId);
}
