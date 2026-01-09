package at.shiftcontrol.shiftservice.event.events.parts;

import java.util.Set;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import at.shiftcontrol.lib.type.NotificationChannel;
import at.shiftcontrol.lib.type.NotificationType;
import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;

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
