package at.shiftcontrol.shiftservice.dto;

import java.util.Set;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.type.NotificationChannel;
import at.shiftcontrol.shiftservice.type.NotificationType;

@Data
@Builder
public class NotificationSettingsDto {
    @NotNull
    private Set<NotificationChannel> notificationChannels;
    @NotNull
    private Set<NotificationType> notificationTypes;
    // could be changed to something like Map<NotificationType, Set<NotificationChannel>> perTypeSettings
    // if per-type channel settings are needed in the future
}
