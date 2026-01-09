package at.shiftcontrol.shiftservice.service.user.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventUpdateDto;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.mapper.UserMapper;
import at.shiftcontrol.shiftservice.service.user.UserAdministrationService;

@Service
@RequiredArgsConstructor
public class UserAdministrationServiceImpl implements UserAdministrationService {
    private final VolunteerDao volunteerDao;
    private final KeycloakUserService keycloakUserService;

    @Override
    @AdminOnly
    public Collection<UserEventDto> getAllUsersForEvent(long eventId) {
        var volunteers = volunteerDao.findAllByEvent(eventId);
        var users = keycloakUserService.getUserById(volunteers.stream().map(Volunteer::getId).toList());
        return UserMapper.toUserEventDto(volunteers, users);
    }

    @Override
    @AdminOnly
    public UserEventDto getUserForEvent(long eventId, String userId) {
        return null;
    }

    @Override
    @AdminOnly
    public Collection<UserEventDto> updateUserForEvent(long eventId, String userId, UserEventUpdateDto updateDto) {
        return List.of();
    }
}
