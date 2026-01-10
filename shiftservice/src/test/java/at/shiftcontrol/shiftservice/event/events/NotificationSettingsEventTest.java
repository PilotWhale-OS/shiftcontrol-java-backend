package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.NotificationSettings;
import at.shiftcontrol.lib.event.events.NotificationSettingsEvent;
import at.shiftcontrol.lib.event.events.parts.NotificationSettingsPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class NotificationSettingsEventTest {

    @Test
    void of() {
        String routingKey = "routingKey";
        String volunteerId = "volunteerId";
        NotificationSettings notificationSettings = mock(NotificationSettings.class);

        NotificationSettingsPart notificationSettingsPart = mock(NotificationSettingsPart.class);
        try (var notificationSettingsPartMock = org.mockito.Mockito.mockStatic(NotificationSettingsPart.class)) {
            notificationSettingsPartMock.when(() -> NotificationSettingsPart.of(notificationSettings)).thenReturn(notificationSettingsPart);

            NotificationSettingsEvent notificationSettingsEvent = NotificationSettingsEvent.of(routingKey, volunteerId, notificationSettings);

            assertEquals(routingKey, notificationSettingsEvent.getRoutingKey());
            assertEquals(volunteerId, notificationSettingsEvent.getVolunteerId());
            assertEquals(notificationSettingsPart, notificationSettingsEvent.getNotificationSettings());
        }
    }
}

