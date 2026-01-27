package at.shiftcontrol.shiftservice.service.userprofile.impl;

import java.util.Collections;
import java.util.Set;

import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.type.NotificationType;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;
import at.shiftcontrol.shiftservice.mapper.UserProfileMapper;
import at.shiftcontrol.shiftservice.service.userprofile.NotificationService;
import at.shiftcontrol.shiftservice.service.userprofile.UserProfileService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final KeycloakUserService kcService;
    private final NotificationService notificationService;
    private final VolunteerDao volunteerDao;
    private final ApplicationUserProvider userProvider;
    private final SecurityHelper securityHelper;

    @Override
    public UserProfileDto getUserProfile(String userId) {
        var currentUser = userProvider.getCurrentUser();
        if (!userId.equals(currentUser.getUserId()) && securityHelper.isNotUserAdmin()) {
            throw new ForbiddenException("Access denied: cannot access other user's profile");
        }

        var user = kcService.getUserById(userId);

        // volunteer data might not yet exist
        Volunteer volunteer = volunteerDao.findById(userId).orElse(null);
        if (volunteer == null) {
            var newVolunteer = Volunteer.builder()
                .id(userId)
                .planningPlans(Collections.emptySet())
                .volunteeringPlans(Collections.emptySet())
                .roles(Collections.emptySet())
                .notificationSettings(Collections.emptySet())
                .build();
            try {
                volunteer = volunteerDao.save(newVolunteer);
            } catch (DataIntegrityViolationException e) {
                // another concurrent request created the same volunteer in the meantime
                volunteer = volunteerDao.getById(userId);
            }
        }

        /* fetch persistent notification settings and add empty set to missing types */
        var notifications = notificationService.getNotificationsForUser(userId);
        for (NotificationType type : NotificationType.values()) {
            if (notifications.stream().noneMatch(n -> n.getType() == type)) {
                notifications.add(NotificationSettingsDto
                    .builder()
                    .type(type)
                    .channels(Set.of())
                    .build()
                );
            }
        }

        return UserProfileMapper.toUserProfileDto(user, notifications, volunteer);
    }
}
