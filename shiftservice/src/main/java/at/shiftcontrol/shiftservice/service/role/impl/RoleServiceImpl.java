package at.shiftcontrol.shiftservice.service.role.impl;

import java.util.Collection;
import java.util.HashSet;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.role.RoleDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.role.RoleDto;
import at.shiftcontrol.shiftservice.dto.role.RoleModificationDto;
import at.shiftcontrol.shiftservice.dto.role.UserRoleAssignmentAssignDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.entity.role.Role;
import at.shiftcontrol.shiftservice.mapper.RoleMapper;
import at.shiftcontrol.shiftservice.mapper.VolunteerMapper;
import at.shiftcontrol.shiftservice.service.role.RoleService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;
import jakarta.ws.rs.NotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleDao roleDao;
    private final ShiftPlanDao shiftPlanDao;
    private final VolunteerDao volunteerDao;
    private final ApplicationUserProvider userProvider;
    private final SecurityHelper securityHelper;

    @Override
    public Collection<RoleDto> getRoles(Long shiftPlanId) {
        return RoleMapper.toRoleDto(roleDao.findAllByShiftPlanId(shiftPlanId));
    }

    @Override
    public RoleDto getRole(Long shiftPlanId, Long roleId) throws ForbiddenException {
        assertUserIsPlanner(shiftPlanId);
        Role role = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        assertRoleBelongsToShiftPlan(role, shiftPlanId);
        return RoleMapper.toRoleDto(role);
    }

    @Override
    public RoleDto createRole(Long shiftPlanId, @NonNull RoleModificationDto roleDto) throws ForbiddenException {
        assertUserIsPlanner(shiftPlanId);
        Role entity = RoleMapper.toRole(roleDto);
        var shiftPlan = shiftPlanDao.findById(shiftPlanId).orElseThrow(NotFoundException::new);
        entity.setShiftPlan(shiftPlan);
        return RoleMapper.toRoleDto(roleDao.save(entity));
    }

    @Override
    public RoleDto updateRole(Long shiftPlanId, Long roleId, @NonNull RoleModificationDto roleDto) throws ForbiddenException {
        assertUserIsPlanner(shiftPlanId);
        Role existing = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        assertRoleBelongsToShiftPlan(existing, shiftPlanId);
        updateRole(roleDto, existing);
        return RoleMapper.toRoleDto(roleDao.save(existing));
    }

    public void updateRole(@NonNull RoleModificationDto roleDto, Role role) {
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());
        role.setSelfAssignable(roleDto.isSelfAssignable());
    }

    @Override
    public void deleteRole(Long shiftPlanId, Long roleId) throws ForbiddenException {
        assertUserIsPlanner(shiftPlanId);
        Role role = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        assertRoleBelongsToShiftPlan(role, shiftPlanId);
        roleDao.delete(role);
    }

    @Override
    public VolunteerDto createUserRoleAssignment(
        Long shiftPlanId,
        String userId,
        UserRoleAssignmentAssignDto assignDto
    ) throws ForbiddenException {
        Role role = roleDao.findById(Long.valueOf(assignDto.getRoleId()))
            .orElseThrow(NotFoundException::new);
        assertRoleBelongsToShiftPlan(role, shiftPlanId);
        Volunteer volunteer = volunteerDao.findById(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found: " + userId));
        if (!role.isSelfAssignable()) {
            assertUserIsPlanner(volunteer, shiftPlanId);
        }
        if (volunteer.getRoles() != null && volunteer.getRoles().stream().anyMatch(r -> r.getId() == role.getId())) {
            throw new ForbiddenException("Role already assigned to user.");
        }
        if (volunteer.getRoles() == null) {
            volunteer.setRoles(new HashSet<>());
        }
        volunteer.getRoles().add(role);
        volunteerDao.save(volunteer);
        return VolunteerMapper.toDto(volunteer);
    }

    @Override
    public void deleteUserRoleAssignment(
        Long shiftPlanId,
        String userId,
        Long roleId
    ) throws ForbiddenException {
        Role role = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        assertRoleBelongsToShiftPlan(role, shiftPlanId);
        Volunteer volunteer = volunteerDao.findById(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found: " + userId));
        if (!role.isSelfAssignable()) {
            assertUserIsPlanner(volunteer, shiftPlanId);
        }
        boolean removed = volunteer.getRoles().removeIf(r -> r.getId() == roleId);
        if (!removed) {
            throw new NotFoundException("Role is not assigned to this user.");
        }
        volunteerDao.save(volunteer);
    }

    private void assertRoleBelongsToShiftPlan(Role role, Long shiftPlanId) throws ForbiddenException {
        if (role.getShiftPlan() == null || role.getShiftPlan().getId() != shiftPlanId) {
            throw new ForbiddenException("User has not the right permission for this shiftPlan.");
        }
    }

    private void assertUserIsPlanner(Long shiftPlanId) throws ForbiddenException {
        String userId = userProvider.getCurrentUser().getUserId();
        Volunteer volunteer = volunteerDao.findById(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found: " + userId));
        assertUserIsPlanner(volunteer, shiftPlanId);
    }

    private void assertUserIsPlanner(Volunteer volunteer, Long shiftPlanId) throws ForbiddenException {
        var shiftPlan = shiftPlanDao.findById(shiftPlanId).orElseThrow(NotFoundException::new);
        securityHelper.assertUserIsPlanner(shiftPlan, volunteer);
    }
}
