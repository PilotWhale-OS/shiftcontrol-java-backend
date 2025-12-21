package at.shiftcontrol.shiftservice.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.dto.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;
import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;
import at.shiftcontrol.shiftservice.service.UserProfileService;
import at.shiftcontrol.shiftservice.type.NotificationChannel;
import at.shiftcontrol.shiftservice.type.NotificationType;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final KeycloakUserService kcService;

    @Override
    public UserProfileDto getUserProfile(String userId) throws NotFoundException {
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

        NotificationSettingsDto notificationsDummy = new NotificationSettingsDto();
        Map<NotificationType, Set<NotificationChannel>> notificationsDummyMap = new HashMap<>();
        notificationsDummyMap.put(NotificationType.SHIFT_REMINDER, new HashSet<>(NotificationChannel.PUSH.ordinal()));
        notificationsDummy.setPerTypeSettings(notificationsDummyMap);
        profile.setNotifications(notificationsDummy);
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
