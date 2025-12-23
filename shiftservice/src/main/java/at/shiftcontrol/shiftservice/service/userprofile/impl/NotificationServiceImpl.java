package at.shiftcontrol.shiftservice.service.userprofile.impl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.NotificationSettingAlreadyExistsException;
import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.entity.VolunteerNotificationAssignment;
import at.shiftcontrol.shiftservice.entity.VolunteerNotificationAssignmentId;
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

    @Override
    public NotificationSettingsDto updateNotificationSetting(String userId, NotificationSettingsDto settingsDto) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(settingsDto, "settingsDto must not be null");
        Objects.requireNonNull(settingsDto.getType(), "settingsDto.type must not be null");
        NotificationType type = settingsDto.getType();
        // normalize channels: null -> empty, remove null entries
        Set<NotificationChannel> requestedChannels = Optional.ofNullable(settingsDto.getChannels())
            .orElseGet(Collections::emptySet)
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(NotificationChannel.class)));
        // load all existing for this (user,type)
        List<VolunteerNotificationAssignment> existingRows =
            notificationRepository.findAllById_VolunteerIdAndId_NotificationType(userId, type);
        Set<NotificationChannel> existingChannels = existingRows.stream()
            .map(VolunteerNotificationAssignment::getNotificationChannel)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(NotificationChannel.class)));
        if (existingChannels.equals(requestedChannels)) {
            throw new NotificationSettingAlreadyExistsException(
                "Notification setting already exists for userId=" + userId
                    + ", type=" + type
                    + ", channels=" + requestedChannels
            );
        }
        // If channels empty:
        // - if no entries: nothing to delete -> just return empty (no exception required per your constraint)
        // - if entries: delete all
        if (requestedChannels.isEmpty()) {
            if (!existingRows.isEmpty()) { // todo redundant?
                notificationRepository.deleteAll(existingRows);
            }
            return NotificationSettingsDto.builder()
                .type(type)
                .channels(EnumSet.noneOf(NotificationChannel.class))
                .build();
        }
        // Diff update: delete removed, add new
        Set<NotificationChannel> toDelete = EnumSet.copyOf(existingChannels);
        toDelete.removeAll(requestedChannels);
        removeExistingChannels(toDelete, existingRows);

        Set<NotificationChannel> toAdd = EnumSet.copyOf(requestedChannels);
        toAdd.removeAll(existingChannels);
        addNewChannels(userId, toAdd, type);

        return fetchPersistedSettings(userId, type);
    }

    private NotificationSettingsDto fetchPersistedSettings(String userId, NotificationType type) {
        Set<NotificationChannel> persistedChannels = notificationRepository.findAllById_VolunteerIdAndId_NotificationType(userId, type)
                .stream()
                .map(VolunteerNotificationAssignment::getNotificationChannel)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(NotificationChannel.class)));
        return NotificationSettingsDto.builder()
            .type(type)
            .channels(persistedChannels)
            .build();
    }

    private void removeExistingChannels(Set<NotificationChannel> toDelete, List<VolunteerNotificationAssignment> existingRows) {
        if (!toDelete.isEmpty()) {
            List<VolunteerNotificationAssignment> deleteEntities = existingRows.stream()
                .filter(r -> toDelete.contains(r.getNotificationChannel()))
                .toList();
            notificationRepository.deleteAll(deleteEntities);
        }
    }

    private void addNewChannels(String userId, Set<NotificationChannel> toAdd, NotificationType type) {
        if (!toAdd.isEmpty()) {
            List<VolunteerNotificationAssignment> addEntities = toAdd.stream()
                .map(ch -> VolunteerNotificationAssignment.builder()
                    .id(VolunteerNotificationAssignmentId.of(userId, type, ch))
                    .build())
                .toList();
            notificationRepository.saveAll(addEntities);
        }
    }
}
