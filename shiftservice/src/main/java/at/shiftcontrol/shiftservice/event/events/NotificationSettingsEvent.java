package at.shiftcontrol.shiftservice.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.NotificationSettingsPart;

@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class NotificationSettingsEvent extends BaseEvent {
    private final String routingKey;

    private final String volunteerId;
    private final NotificationSettingsPart notificationSettings;

    public static NotificationSettingsEvent of(String routingKey, String volunteerId, NotificationSettingsDto notificationSettings) {
        return new NotificationSettingsEvent(routingKey, volunteerId, NotificationSettingsPart.of(notificationSettings));
    }
}
