package at.shiftcontrol.shiftservice.event.events.parts;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.type.NotificationChannel;
import at.shiftcontrol.lib.type.NotificationType;
import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationSettingsPartTest {

    @Test
    void of() {
        // Arrange
        NotificationSettingsDto dto = new NotificationSettingsDto();
        dto.setType(NotificationType.SHIFT_REMINDER);
        dto.setChannels(Collections.singleton(NotificationChannel.EMAIL));

        // Act
        NotificationSettingsPart part = NotificationSettingsPart.of(dto);

        // Assert
        assertEquals(dto.getType(), part.getType());
        assertEquals(dto.getChannels(), part.getChannels());
    }

    @Test
    void of_withMultipleChannels() {
        // Arrange
        NotificationSettingsDto dto = new NotificationSettingsDto();
        dto.setType(NotificationType.SHIFT_REMINDER);
        dto.setChannels(Set.of(NotificationChannel.EMAIL, NotificationChannel.PUSH));

        // Act
        NotificationSettingsPart part = NotificationSettingsPart.of(dto);

        // Assert
        assertEquals(dto.getType(), part.getType());
        assertEquals(dto.getChannels(), part.getChannels());
    }
}

