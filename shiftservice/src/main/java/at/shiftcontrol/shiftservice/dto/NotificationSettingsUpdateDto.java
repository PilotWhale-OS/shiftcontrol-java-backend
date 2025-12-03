package at.shiftcontrol.shiftservice.dto;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.type.NotificationChannel;
import at.shiftcontrol.shiftservice.type.NotificationType;

@Data
@Builder
public class NotificationSettingsUpdateDto {
    private Set<NotificationChannel> notificationChannels;
    private Set<NotificationType> notificationTypes;
}
