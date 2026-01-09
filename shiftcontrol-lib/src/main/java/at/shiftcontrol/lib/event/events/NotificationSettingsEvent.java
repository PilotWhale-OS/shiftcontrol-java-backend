package at.shiftcontrol.lib.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.NotificationSettings;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.events.parts.NotificationSettingsPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationSettingsEvent extends BaseEvent {
    private final String volunteerId;
    private final NotificationSettingsPart notificationSettings;

    public NotificationSettingsEvent(String routingKey, String volunteerId, NotificationSettingsPart notificationSettings) {
        super(routingKey);
        this.volunteerId = volunteerId;
        this.notificationSettings = notificationSettings;
    }

    public static NotificationSettingsEvent of(String routingKey, String volunteerId, NotificationSettings notificationSettings) {
        return new NotificationSettingsEvent(routingKey, volunteerId, NotificationSettingsPart.of(notificationSettings));
    }
}
