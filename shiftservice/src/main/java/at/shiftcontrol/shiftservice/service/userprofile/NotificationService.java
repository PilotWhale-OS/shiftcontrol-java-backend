package at.shiftcontrol.shiftservice.service.userprofile;

import java.util.Set;

import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;

public interface NotificationService {
    Set<NotificationSettingsDto> getNotificationsForUser(String userId);

    NotificationSettingsDto updateNotificationSetting(String userId, NotificationSettingsDto settingsDto);
}
