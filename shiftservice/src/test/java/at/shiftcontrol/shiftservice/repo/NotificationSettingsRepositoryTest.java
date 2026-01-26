package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.NotificationSettings;
import at.shiftcontrol.lib.type.NotificationChannel;
import at.shiftcontrol.lib.type.NotificationType;
import at.shiftcontrol.shiftservice.repo.userprofile.NotificationSettingsRepository;

@DataJpaTest
@Import({TestConfig.class})
public class NotificationSettingsRepositoryTest {
    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    @Test
    void testGetAllVolunteerNotificationSettings() {
        List<NotificationSettings> notificationSettings = notificationSettingsRepository.findAll();
        Assertions.assertFalse(notificationSettings.isEmpty());
    }

    @Test
    void testFindAllByUserId() {
        Collection<NotificationSettings> notificationSettings = notificationSettingsRepository.findAllByUserId("28c02050-4f90-4f3a-b1df-3c7d27a166e8");
        Assertions.assertFalse(notificationSettings.isEmpty());
    }

    @Test
    void testFindAllByNotificationTypeAndChannel() {
        Collection<String> recipients = notificationSettingsRepository.findAllByNotificationTypeAndChannel(
            NotificationType.ADMIN_TRUST_ALERT_RECEIVED, NotificationChannel.EMAIL
        );
        Assertions.assertFalse(recipients.isEmpty());
        Assertions.assertEquals("28c02050-4f90-4f3a-b1df-3c7d27a166e8", recipients.stream().findFirst().get());
    }

    @Test
    void testFindAllByNotificationTypeAndChannelDisabled() {
        Collection<String> recipients = notificationSettingsRepository.findAllByNotificationTypeAndChannelDisabled(
            NotificationType.ADMIN_REWARD_SYNC_USED, NotificationChannel.PUSH, List.of("28c02050-4f90-4f3a-b1df-3c7d27a166e8"));
        Assertions.assertFalse(recipients.isEmpty());
        Assertions.assertEquals("28c02050-4f90-4f3a-b1df-3c7d27a166e8", recipients.stream().findFirst().get());
    }

    @Test
    void testFindAllByNotificationTypeAndChannelAndUserIds() {
        Collection<String> recipients = notificationSettingsRepository.findAllByNotificationTypeAndChannelAndUserIds(
            NotificationType.ADMIN_TRUST_ALERT_RECEIVED, NotificationChannel.EMAIL, List.of("28c02050-4f90-4f3a-b1df-3c7d27a166e8"));
        Assertions.assertFalse(recipients.isEmpty());
        Assertions.assertEquals("28c02050-4f90-4f3a-b1df-3c7d27a166e8", recipients.stream().findFirst().get());
    }
}
