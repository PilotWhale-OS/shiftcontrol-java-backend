package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.dto.userprofile.NotificationSettingsDto;
import at.shiftcontrol.lib.event.events.NotificationSettingsEvent;
import at.shiftcontrol.lib.event.events.parts.NotificationSettingsPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class NotificationSettingsEventTest {

    @Test
    void of() {
        String routingKey = "routingKey";
        String volunteerId = "volunteerId";
        NotificationSettingsDto notificationSettingsDto = mock(NotificationSettingsDto.class);

        NotificationSettingsPart notificationSettingsPart = mock(NotificationSettingsPart.class);
        try (var notificationSettingsPartMock = org.mockito.Mockito.mockStatic(NotificationSettingsPart.class)) {
            notificationSettingsPartMock.when(() -> NotificationSettingsPart.of(notificationSettingsDto)).thenReturn(notificationSettingsPart);

            NotificationSettingsEvent notificationSettingsEvent = NotificationSettingsEvent.of(routingKey, volunteerId, notificationSettingsDto);

            assertEquals(routingKey, notificationSettingsEvent.getRoutingKey());
            assertEquals(volunteerId, notificationSettingsEvent.getVolunteerId());
            assertEquals(notificationSettingsPart, notificationSettingsEvent.getNotificationSettings());
        }
    }
}

