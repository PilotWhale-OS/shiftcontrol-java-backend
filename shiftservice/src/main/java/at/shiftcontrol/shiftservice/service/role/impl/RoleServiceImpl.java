package at.shiftcontrol.shiftservice.service.role.impl;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.NotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleDao roleDao;
    private final ShiftPlanDao shiftPlanDao;
    private final VolunteerDao volunteerDao;
    private final ApplicationUserProvider userProvider;
    private final SecurityHelper securityHelper;
    private final ApplicationEventPublisher publisher;

    @Override
    public Collection<RoleDto> getRoles(Long shiftPlanId) {
        return RoleMapper.toRoleDto(roleDao.findAllByShiftPlanId(shiftPlanId));
    }

    @Override
    public RoleDto getRole(Long roleId) throws ForbiddenException {
        Role role = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        securityHelper.assertUserIsPlanner(role.getShiftPlan().getId());
        return RoleMapper.toRoleDto(role);
    }

    @Override
    public RoleDto createRole(Long shiftPlanId, @NonNull RoleModificationDto roleDto) throws ForbiddenException {
        securityHelper.assertUserIsPlanner(shiftPlanId);
        Role entity = RoleMapper.toRole(roleDto);
        var shiftPlan = shiftPlanDao.findById(shiftPlanId).orElseThrow(NotFoundException::new);
        entity.setShiftPlan(shiftPlan);

        //TODO publish event
        return RoleMapper.toRoleDto(roleDao.save(entity));
    }

    @Override
    public RoleDto updateRole(Long roleId, @NonNull RoleModificationDto roleDto) throws ForbiddenException {
        Role existing = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        securityHelper.assertUserIsPlanner(existing.getShiftPlan().getId());
        updateRole(roleDto, existing);

        //TODO publish event
        return RoleMapper.toRoleDto(roleDao.save(existing));
    }

    private void updateRole(@NonNull RoleModificationDto roleDto, Role role) {
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());
        role.setSelfAssignable(roleDto.isSelfAssignable());
    }

    @Override
    public void deleteRole(Long roleId) throws ForbiddenException {
        Role role = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        securityHelper.assertUserIsPlanner(role.getShiftPlan().getId());

        //TODO publish event
        roleDao.delete(role);
    }

    @Override
    public VolunteerDto createUserRoleAssignment(String userId, UserRoleAssignmentAssignDto assignDto) throws ForbiddenException {
        Role role = roleDao.findById(Long.valueOf(assignDto.getRoleId()))
            .orElseThrow(NotFoundException::new);
        Volunteer volunteer = volunteerDao.findById(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found: " + userId));
        if (!role.isSelfAssignable()) {
            securityHelper.assertUserIsPlanner(role.getShiftPlan().getId());
        }
        if (volunteer.getRoles() != null && volunteer.getRoles().stream().anyMatch(r -> r.getId() == role.getId())) {
            throw new ForbiddenException("Role already assigned to user.");
        }
        if (volunteer.getRoles() == null) {
            volunteer.setRoles(new HashSet<>());
        }
        volunteer.getRoles().add(role);
        volunteerDao.save(volunteer);

        //TODO publish event
        return VolunteerMapper.toDto(volunteer);
    }

    @Override
    public void deleteUserRoleAssignment(String userId, Long roleId) throws ForbiddenException {
        Role role = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        Volunteer volunteer = volunteerDao.findById(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found: " + userId));
        if (!role.isSelfAssignable()) {
            securityHelper.assertUserIsPlanner(role.getShiftPlan().getId());
        }
        boolean removed = volunteer.getRoles().removeIf(r -> r.getId() == roleId);
        if (!removed) {
            throw new NotFoundException("Role is not assigned to this user.");
        }

        //TODO publish event
        volunteerDao.save(volunteer);
    }
}
