package at.shiftcontrol.shiftservice.service.userprofile.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;
import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;
import at.shiftcontrol.shiftservice.service.userprofile.NotificationService;
import at.shiftcontrol.shiftservice.service.userprofile.UserProfileService;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final KeycloakUserService kcService;
    private final NotificationService notificationService;

    @Override
    public UserProfileDto getUserProfile(String userId) {
        var user = kcService.getUserById(userId);
        var account = new AccountInfoDto();

        var userTypeAttr = user.firstAttribute("userType");
        var userType = userTypeAttr == null ? UserType.ASSIGNED : UserType.valueOf(userTypeAttr);

        account.setUserType(userType);
        account.setId(user.getId());
        account.setUsername(user.getUsername());
        account.setFistName(user.getFirstName());
        account.setLastName(user.getLastName());
        account.setEmail(user.getEmail());

        var profile = new UserProfileDto();
        profile.setAccount(account);
        profile.setNotifications(notificationService.getNotificationsForUser(userId));

        return profile;
    }
    //     public NotificationSettingsDto updateNotificationSetting(NotificationSettingsDto settingsDto){
    //             var volunteer = dao.findById(setting.getUserId()).orElseThrow(() -> new NotFoundException("Volunteer not found"));
    //             var currentNotifications = volunteer.getNotificationAssignments();
    //         settingsDto.getPerTypeSettings().forEach(setting -> {
    //             if (currentNotifications.contains(setting)) {
    //
    //             }
    //
    //             dao.save(volunteer);
    //         });
    //
    //
    //         return RoleMapper.toNotificationSettingsDto()
    //     }
}
