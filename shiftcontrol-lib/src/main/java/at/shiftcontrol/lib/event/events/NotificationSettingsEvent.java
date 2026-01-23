package at.shiftcontrol.lib.event.events;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.NotificationSettings;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.RoutingKeys;
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

    public static NotificationSettingsEvent ofInternal(String routingKey, String volunteerId, NotificationSettings notificationSettings) {
        return new NotificationSettingsEvent(routingKey, volunteerId, NotificationSettingsPart.of(notificationSettings));
    }

    public static NotificationSettingsEvent settingsUpdated(String volunteerId, NotificationSettings notificationSettings) {
        return ofInternal(RoutingKeys.format(RoutingKeys.VOLUNTEER_NOTIFICATION_PREFERENCE_UPDATED,
                Map.of("volunteerId", volunteerId)),
            volunteerId, notificationSettings);
    }
}
