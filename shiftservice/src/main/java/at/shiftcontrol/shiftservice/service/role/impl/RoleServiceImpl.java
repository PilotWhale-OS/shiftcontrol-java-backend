package at.shiftcontrol.shiftservice.service.role.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

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
import at.shiftcontrol.shiftservice.event.RoutingKeys;
import at.shiftcontrol.shiftservice.event.events.RoleEvent;
import at.shiftcontrol.shiftservice.event.events.RoleVolunteerEvent;
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
        var shiftPlan = shiftPlanDao.findById(shiftPlanId).orElseThrow(NotFoundException::new);

        var role = RoleMapper.toRole(roleDto);
        role.setShiftPlan(shiftPlan);

        publisher.publishEvent(RoleEvent.of(RoutingKeys.ROLE_CREATED, role));
        return RoleMapper.toRoleDto(roleDao.save(role));
    }

    @Override
    public RoleDto updateRole(Long roleId, @NonNull RoleModificationDto roleDto) throws ForbiddenException {
        var role = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        securityHelper.assertUserIsPlanner(role.getShiftPlan().getId());

        updateRole(roleDto, role);
        role = roleDao.save(role);

        publisher.publishEvent(RoleEvent.of(RoutingKeys.format(RoutingKeys.ROLE_UPDATED,
            Map.of("roleId", roleId.toString())), role));
        return RoleMapper.toRoleDto(role);
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

        publisher.publishEvent(RoleEvent.of(RoutingKeys.format(RoutingKeys.ROLE_DELETED,
            Map.of("roleId", roleId.toString())), role));
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

        publisher.publishEvent(RoleVolunteerEvent.of(RoutingKeys.format(RoutingKeys.ROLE_ASSIGNED,
            Map.of("roleId", String.valueOf(role.getId()), "volunteerId", volunteer.getId())), role, volunteer.getId()));
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

        publisher.publishEvent(RoleVolunteerEvent.of(RoutingKeys.format(RoutingKeys.ROLE_UNASSIGNED,
            Map.of("roleId", String.valueOf(role.getId()), "volunteerId", volunteer.getId())), role, volunteer.getId()));
        volunteerDao.save(volunteer);
    }
}
