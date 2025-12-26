package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;

import org.springframework.stereotype.Service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.role.RoleAssignmentDao;
import at.shiftcontrol.shiftservice.dao.role.RoleDao;
import at.shiftcontrol.shiftservice.dto.roles.RoleAssignmentAssignDto;
import at.shiftcontrol.shiftservice.dto.roles.RoleAssignmentDto;
import at.shiftcontrol.shiftservice.dto.roles.RoleDto;
import at.shiftcontrol.shiftservice.dto.roles.RoleModificationDto;
import at.shiftcontrol.shiftservice.entity.role.Role;
import at.shiftcontrol.shiftservice.entity.role.RoleAssignment;
import at.shiftcontrol.shiftservice.mapper.RoleMapper;
import at.shiftcontrol.shiftservice.service.RoleService;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleDao roleDao;
    private final ShiftPlanDao shiftPlanDao;
    private final RoleAssignmentDao roleAssignmentDao;

    @Override
    public Collection<RoleDto> getRoles(Long shiftPlanId) {
        return RoleMapper.toRoleDto(roleDao.findAllByShiftPlanId(shiftPlanId));
    }

    @Override
    public RoleDto getRole(Long shiftPlanId, Long roleId) throws ForbiddenException {
        Role role = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        assertRoleBelongsToEvent(role, shiftPlanId);
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
        assertRoleBelongsToEvent(existing, shiftPlanId);
        // update fields (avoid overwriting shiftPlanId accidentally if that's immutable)
        existing.setName(roleDto.getName());
        existing.setDescription(roleDto.getDescription());
        Role saved = roleDao.save(existing);
        return RoleMapper.toRoleDto(saved);
    }

    @Override
    public void deleteRole(Long shiftPlanId, Long roleId) throws ForbiddenException {
        Role role = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        assertRoleBelongsToEvent(role, shiftPlanId);
        roleDao.delete(role);
    }

    @Override
    public Collection<RoleAssignmentDto> getRoleAssignmentsForUser(Long shiftPlanId, String userId) {
        return RoleMapper.toRoleAssignmentDto(
            roleAssignmentDao.findAllByRole_ShiftPlanIdAndUserId(shiftPlanId, userId)
        );
    }

    @Override
    public RoleAssignmentDto createRoleAssignment(Long shiftPlanId, String userId, RoleAssignmentAssignDto assignDto) throws ForbiddenException {
        Role role = roleDao.findById(Long.valueOf(assignDto.getRoleId())).orElseThrow(NotFoundException::new);
        assertRoleBelongsToEvent(role, shiftPlanId);
        RoleAssignment assignment = RoleAssignment.builder()
            .userId(userId)
            .role(role)
            .build();
        roleAssignmentDao.save(assignment);
        return RoleMapper.toRoleAssignmentDto(assignment);
    }

    @Override
    public void deleteRoleAssignment(Long shiftPlanId, String userId, Long roleAssignmentId) throws ForbiddenException {
        var assignment = roleAssignmentDao.findById(roleAssignmentId).orElseThrow(NotFoundException::new);
        assertRoleBelongsToEvent(assignment.getRole(), shiftPlanId);
        if (!assignment.getUserId().equals(userId)) {
            throw new ForbiddenException("User does not match user of assignment.");
        }
        roleAssignmentDao.delete(assignment);
    }

    private void assertRoleBelongsToEvent(Role role, Long shiftPlanId) throws ForbiddenException {
        if (role.getShiftPlan().getId() != shiftPlanId) {
            throw new ForbiddenException("User has not the right permission for this shiftPlan.");
        }
    }
}
