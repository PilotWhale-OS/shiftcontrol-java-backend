package at.shiftcontrol.shiftservice.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import at.shiftcontrol.lib.type.NotificationChannel;
import at.shiftcontrol.lib.type.NotificationType;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.dto.notifications.RecipientsDto;
import at.shiftcontrol.shiftservice.dto.notifications.RecipientsFilterDto;
import at.shiftcontrol.shiftservice.type.ReceiverAccessLevel;
import at.shiftcontrol.shiftservice.util.TestEntityFactory;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@WithMockUser(authorities = "ADMIN")
public class NotificationRecipientServiceTest {

    @Autowired
    NotificationRecipientServiceImpl notificationRecipientService;
    @Autowired
    TestEntityFactory testEntityFactory;
    @MockitoBean
    KeycloakUserService keycloakUserService;

    @Test
    void testGetRecipientsForNotificationAdminTest() {
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e8";
        RecipientsFilterDto filter = RecipientsFilterDto.builder()
            .notificationType(NotificationType.ADMIN_PLANNER_JOINED_PLAN)
            .notificationChannel(NotificationChannel.PUSH)
            .receiverAccessLevel(ReceiverAccessLevel.ADMIN)
            .relatedShiftPlanId("4")
            .build();
        Mockito.when(keycloakUserService.getAllAdmins())
            .thenReturn(List.of(testEntityFactory.getUserRepresentationWithId(userId)));
        Mockito.when(keycloakUserService.getUserByIds(any()))
            .thenReturn(List.of(testEntityFactory.getUserRepresentationWithId(userId)));

        RecipientsDto recipientsDto = notificationRecipientService.getRecipientsForNotification(filter);

        Assertions.assertNotNull(recipientsDto);
        Assertions.assertFalse(recipientsDto.getRecipients().isEmpty());
    }

    @Test
    void testGetRecipientsForNotificationAdmin() {
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e8";
        RecipientsFilterDto filter = RecipientsFilterDto.builder()
            .notificationType(NotificationType.ADMIN_TRUST_ALERT_RECEIVED)
            .notificationChannel(NotificationChannel.PUSH)
            .receiverAccessLevel(ReceiverAccessLevel.ADMIN)
            .build();
        Mockito.when(keycloakUserService.getAllAdmins())
            .thenReturn(List.of(testEntityFactory.getUserRepresentationWithId(userId)));
        Mockito.when(keycloakUserService.getUserByIds(any()))
            .thenReturn(List.of(testEntityFactory.getUserRepresentationWithId(userId)));

        RecipientsDto recipientsDto = notificationRecipientService.getRecipientsForNotification(filter);

        Assertions.assertNotNull(recipientsDto);
        Assertions.assertFalse(recipientsDto.getRecipients().isEmpty());
    }

    @Test
    void testGetRecipientsForNotificationVolunteer() {
        String userId = "28c02050-4f90-4f3a-b1df-3c7d27a166e8";
        RecipientsFilterDto filter = RecipientsFilterDto.builder()
            .notificationType(NotificationType.VOLUNTEER_PLANS_CHANGED)
            .notificationChannel(NotificationChannel.EMAIL)
            .receiverAccessLevel(ReceiverAccessLevel.VOLUNTEER)
            .relatedShiftPlanId("2")
            .build();
        Mockito.when(keycloakUserService.getUserByIds(any()))
            .thenReturn(List.of(testEntityFactory.getUserRepresentationWithId(userId)));

        RecipientsDto recipientsDto = notificationRecipientService.getRecipientsForNotification(filter);

        Assertions.assertNotNull(recipientsDto);
        Assertions.assertFalse(recipientsDto.getRecipients().isEmpty());
    }
}
