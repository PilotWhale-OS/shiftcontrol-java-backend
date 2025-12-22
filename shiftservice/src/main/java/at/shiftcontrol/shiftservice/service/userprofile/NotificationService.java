package at.shiftcontrol.shiftservice.service.userprofile;

import java.util.Set;

import at.shiftcontrol.shiftservice.dto.NotificationSettingsDto;

public interface NotificationService {
    Set<NotificationSettingsDto> getNotificationsForUser(String userId);
}
