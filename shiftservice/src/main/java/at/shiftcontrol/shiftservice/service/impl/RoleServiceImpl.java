package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;

import org.springframework.stereotype.Service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.shiftservice.dao.EventDao;
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
    private final EventDao eventDao;
    private final RoleAssignmentDao roleAssignmentDao;

    @Override
    public Collection<RoleDto> getRoles(Long eventId) {
        return RoleMapper.toRoleDto(roleDao.findAllByEventId(eventId));
    }

    @Override
    public RoleDto getRole(Long eventId, Long roleId) throws ForbiddenException {
        Role role = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        assertRoleBelongsToEvent(role, eventId);
        return RoleMapper.toRoleDto(role);
    }

    @Override
    public RoleDto createRole(Long eventId, RoleModificationDto roleDto) {
        Role entity = RoleMapper.toRole(roleDto);
        var event = eventDao.findById(eventId).orElseThrow(NotFoundException::new);
        entity.setEvent(event);
        return RoleMapper.toRoleDto(roleDao.save(entity));
    }

    @Override
    public RoleDto updateRole(Long eventId, Long roleId, RoleModificationDto roleDto) throws ForbiddenException {
        Role existing = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        assertRoleBelongsToEvent(existing, eventId);
        // update fields (avoid overwriting eventId accidentally if that's immutable)
        existing.setName(roleDto.getName());
        existing.setDescription(roleDto.getDescription());
        Role saved = roleDao.save(existing);
        return RoleMapper.toRoleDto(saved);
    }

    @Override
    public void deleteRole(Long eventId, Long roleId) throws ForbiddenException {
        Role role = roleDao.findById(roleId).orElseThrow(NotFoundException::new);
        assertRoleBelongsToEvent(role, eventId);
        roleDao.delete(role);
    }

    @Override
    public Collection<RoleAssignmentDto> getRoleAssignmentsForUser(Long eventId, String userId) {
        return RoleMapper.toRoleAssignmentDto(
            roleAssignmentDao.findAllByRole_EventIdAndUserId(eventId, userId)
        );
    }

    @Override
    public RoleAssignmentDto createRoleAssignment(Long eventId, String userId, RoleAssignmentAssignDto assignDto) throws ForbiddenException {
        Role role = roleDao.findById(Long.valueOf(assignDto.getRoleId())).orElseThrow(NotFoundException::new);
        assertRoleBelongsToEvent(role, eventId);
        RoleAssignment assignment = RoleAssignment.builder()
            .userId(userId)
            .role(role)
            .build();
        roleAssignmentDao.save(assignment);
        return RoleMapper.toRoleAssignmentDto(assignment);
    }

    @Override
    public void deleteRoleAssignment(Long eventId, String userId, Long roleAssignmentId) throws ForbiddenException {
        var assignment = roleAssignmentDao.findById(roleAssignmentId).orElseThrow(NotFoundException::new);
        assertRoleBelongsToEvent(assignment.getRole(), eventId);
        if (!assignment.getUserId().equals(userId)) {
            throw new ForbiddenException("User does not match user of assignment.");
        }
        roleAssignmentDao.delete(assignment);
    }

    private void assertRoleBelongsToEvent(Role role, Long eventid) throws ForbiddenException {
        if (role.getEvent().getId() != eventid) {
            throw new ForbiddenException("User has not the right permission for this event.");
        }
    }
}
