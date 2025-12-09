package at.shiftcontrol.shiftservice.dto;

import java.util.Map;
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
    private Map<NotificationType, Set<NotificationChannel>> perTypeSettings;
}
