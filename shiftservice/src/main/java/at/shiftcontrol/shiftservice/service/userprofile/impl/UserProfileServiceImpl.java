package at.shiftcontrol.shiftservice.service.userprofile.impl;

import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.type.NotificationType;

import org.antlr.v4.runtime.atn.SemanticContext;

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

import java.util.Set;

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

        /* fetch persistent notification settings and add empty set to missing types */
        var notifications = notificationService.getNotificationsForUser(userId);
        for(NotificationType type : NotificationType.values()) {
            if(notifications.stream().noneMatch(n -> n.getType() == type)) {
                notifications.add(NotificationSettingsDto
                    .builder()
                    .type(type)
                    .channels(Set.of())
                    .build()
                );
            }
        }

        profile.setNotifications(notifications);
        profile.setAssignedRoles(RoleMapper.toRoleDto(volunteer.getRoles()));

        return profile;
    }
}
