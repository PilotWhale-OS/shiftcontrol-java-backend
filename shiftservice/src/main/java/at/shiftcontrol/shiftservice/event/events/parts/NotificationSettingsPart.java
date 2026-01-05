package at.shiftcontrol.shiftservice.event.events.parts;

import java.util.Set;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.type.NotificationChannel;
import at.shiftcontrol.shiftservice.type.NotificationType;

@AllArgsConstructor
@Data
public class NotificationSettingsPart {
    @NotNull
    private NotificationType type;
    @NotNull
    private Set<NotificationChannel> channels;

    public static NotificationSettingsPart of(NotificationSettingsDto dto) {
        return new NotificationSettingsPart(
            dto.getType(),
            dto.getChannels()
        );
    }
}
