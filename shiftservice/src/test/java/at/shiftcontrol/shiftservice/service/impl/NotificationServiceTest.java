package at.shiftcontrol.shiftservice.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.type.NotificationChannel;
import at.shiftcontrol.lib.type.NotificationType;
import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.service.userprofile.impl.NotificationServiceImpl;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class NotificationServiceTest {

    @Autowired
    NotificationServiceImpl notificationService;

    @Test
    void testUpdateNotificationSetting() {
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e8";
        NotificationSettingsDto settingsDto = NotificationSettingsDto.builder()
            .type(NotificationType.VOLUNTEER_AUTO_ASSIGNED)
            .channels(Set.of(NotificationChannel.PUSH))
            .build();

        NotificationSettingsDto savedDto = notificationService.updateNotificationSetting(userId, settingsDto);

        Assertions.assertEquals(settingsDto, savedDto);
    }
}
