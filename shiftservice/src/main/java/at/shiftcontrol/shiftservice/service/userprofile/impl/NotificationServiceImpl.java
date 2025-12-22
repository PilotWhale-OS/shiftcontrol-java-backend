package at.shiftcontrol.shiftservice.service.userprofile.impl;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dto.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.entity.VolunteerNotificationAssignment;
import at.shiftcontrol.shiftservice.repo.userprofile.NotificationRepository;
import at.shiftcontrol.shiftservice.service.userprofile.NotificationService;
import at.shiftcontrol.shiftservice.type.NotificationChannel;
import at.shiftcontrol.shiftservice.type.NotificationType;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    @Override
    public Set<NotificationSettingsDto> getNotificationsForUser(String userId) {
        List<VolunteerNotificationAssignment> rows = notificationRepository.findAllById_VolunteerId(userId);
        Map<NotificationType, Set<NotificationChannel>> grouped =
            rows.stream()
                .collect(Collectors.groupingBy(
                    VolunteerNotificationAssignment::getNotificationType,
                    Collectors.mapping(
                        VolunteerNotificationAssignment::getNotificationChannel,
                        Collectors.toCollection(() -> EnumSet.noneOf(NotificationChannel.class))
                    )
                ));
        return grouped.entrySet().stream()
            .map(entry -> NotificationSettingsDto.builder()
                .type(entry.getKey())
                .channels(entry.getValue())
                .build()
            )
            .collect(Collectors.toSet());
    }
}
