package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.role.RoleDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.role.RoleDto;
import at.shiftcontrol.shiftservice.dto.role.RoleModificationDto;
import at.shiftcontrol.shiftservice.dto.role.UserRoleAssignmentAssignDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.entity.role.Role;
import at.shiftcontrol.shiftservice.mapper.RoleMapper;
import at.shiftcontrol.shiftservice.mapper.VolunteerMapper;
import at.shiftcontrol.shiftservice.service.RoleService;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleDao roleDao;
    private final ShiftPlanDao shiftPlanDao;
    private final VolunteerDao volunteerDao;

    @Override
    public Collection<RoleDto> getRoles(Long shiftPlanId) {
        return RoleMapper.toRoleDto(roleDao.findAllByShiftPlanId(shiftPlanId));
    }

    @Override
    public RoleDto getRole(Long shiftPlanId, Long roleId) throws ForbiddenException {
        Role role = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        assertRoleBelongsToShiftPlan(role, shiftPlanId);
        return RoleMapper.toRoleDto(role);
    }

    @Override
    public RoleDto createRole(Long shiftPlanId, RoleModificationDto roleDto) {
        Role entity = RoleMapper.toRole(roleDto);
        var shiftPlan = shiftPlanDao.findById(shiftPlanId).orElseThrow(NotFoundException::new);
        entity.setShiftPlan(shiftPlan);
        return RoleMapper.toRoleDto(roleDao.save(entity));
    }

    @Override
    public RoleDto updateRole(Long shiftPlanId, Long roleId, RoleModificationDto roleDto) throws ForbiddenException {
        Role existing = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        assertRoleBelongsToShiftPlan(existing, shiftPlanId);
        RoleMapper.updateRole(roleDto, existing);
        return RoleMapper.toRoleDto(roleDao.save(existing));
    }

    @Override
    public void deleteRole(Long shiftPlanId, Long roleId) throws ForbiddenException {
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
            if (volunteer.getPlanningPlans() == null) {
                throw new ForbiddenException();
            }
            var shiftPlan = shiftPlanDao.findById(shiftPlanId).orElseThrow(NotFoundException::new);
            List<@NotNull Event> planningPlans = volunteer
                .getPlanningPlans()
                .stream()
                .map(ShiftPlan::getEvent)
                .toList();
            if (!planningPlans.contains(shiftPlan.getEvent())) {
                throw new ForbiddenException();
            }
        }

        if (volunteer.getRoles() != null && volunteer.getRoles().stream().anyMatch(r -> r.getId() == role.getId())) {
            throw new ForbiddenException("Role already assigned to user.");
        }
        if (volunteer.getRoles() == null) {
            volunteer.setRoles(new HashSet<>());
        }
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
}
