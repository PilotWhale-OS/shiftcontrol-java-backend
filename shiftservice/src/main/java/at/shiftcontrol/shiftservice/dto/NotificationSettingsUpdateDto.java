package at.shiftcontrol.shiftservice.dto;

import at.shiftcontrol.shiftservice.type.NotificationChannel;
import at.shiftcontrol.shiftservice.type.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class NotificationSettingsUpdateDto {
    private Set<NotificationChannel> notificationChannels;
    private Set<NotificationType> notificationTypes;
}
