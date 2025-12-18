package at.shiftcontrol.shiftservice.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.dao.UserProfile.UserProfileDao;
import at.shiftcontrol.shiftservice.dto.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.dto.UserProfile.AccountInfoDto;
import at.shiftcontrol.shiftservice.dto.UserProfile.UserProfileDto;
import at.shiftcontrol.shiftservice.service.UserProfileService;
import at.shiftcontrol.shiftservice.type.NotificationChannel;
import at.shiftcontrol.shiftservice.type.NotificationType;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final UserProfileDao dao;
    private final KeycloakUserService kcService;

    @Override
    public UserProfileDto getUserProfile(Long userId) throws NotFoundException {
        var dummy = new UserProfileDto();
        AccountInfoDto dummyAccount = new AccountInfoDto();
        dummyAccount.setId("1");
        dummyAccount.setUsername("testUser");
        dummyAccount.setEmail("testUser@dummy.at");
        dummy.setAccount(dummyAccount);
        NotificationSettingsDto notificationsDummy = new NotificationSettingsDto();
        Map<NotificationType, Set<NotificationChannel>> notificationsDummyMap = new HashMap<>();
        notificationsDummyMap.put(NotificationType.SHIFT_REMINDER, new HashSet<>(NotificationChannel.PUSH.ordinal()));
        notificationsDummy.setPerTypeSettings(notificationsDummyMap);
        dummy.setNotifications(notificationsDummy);
        return dummy;
//         var accountInfo = kcService.getUserById(userId);
//
//         var profile = dao.findSettingsByUserId(userId).orElse(null); // todo what is the result when there are no settings or absences?
//         profile.setAccount(new AccountInfoDto(accountInfo.getId(), accountInfo.getUsername(), accountInfo.getEmail()));
//         // todo rollen kommen auch von keycloak?
//         return profile;
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
