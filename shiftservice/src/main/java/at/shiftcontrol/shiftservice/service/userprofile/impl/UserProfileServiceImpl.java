package at.shiftcontrol.shiftservice.service.userprofile.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;
import at.shiftcontrol.shiftservice.mapper.RoleMapper;
import at.shiftcontrol.shiftservice.mapper.UserProfileMapper;
import at.shiftcontrol.shiftservice.service.userprofile.NotificationService;
import at.shiftcontrol.shiftservice.service.userprofile.UserProfileService;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final KeycloakUserService kcService;
    private final NotificationService notificationService;
    private final VolunteerDao volunteerDao;

    @Override
    public UserProfileDto getUserProfile(String userId) throws NotFoundException {
        var user = kcService.getUserById(userId);
        var volunteer = volunteerDao.findByUserId(userId).orElseThrow(NotFoundException::new);

        var profile = new UserProfileDto();
        profile.setAccount(UserProfileMapper.toAccountInfoDto(user));
        profile.setNotifications(notificationService.getNotificationsForUser(userId));
        profile.setAssignedRoles(RoleMapper.toRoleDto(volunteer.getRoles()));

        return profile;
    }
}
