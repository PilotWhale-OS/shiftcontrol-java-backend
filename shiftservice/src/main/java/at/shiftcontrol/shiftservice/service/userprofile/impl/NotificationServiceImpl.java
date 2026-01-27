package at.shiftcontrol.shiftservice.service.userprofile.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.NotificationSettings;
import at.shiftcontrol.lib.event.events.NotificationSettingsEvent;
import at.shiftcontrol.lib.type.NotificationChannel;
import at.shiftcontrol.lib.type.NotificationType;
import at.shiftcontrol.shiftservice.dao.NotificationSettingsDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.mapper.NotificationSettingsMapper;
import at.shiftcontrol.shiftservice.service.userprofile.NotificationService;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationSettingsDao notificationSettingsDao;
    private final VolunteerDao volunteerDao;
    private final ApplicationEventPublisher publisher;

    @Override
    public Collection<NotificationSettingsDto> getNotificationsForUser(String userId) {
        return NotificationSettingsMapper.toDto(getSettingsOrDefault(userId));
    }

    @Override
    public NotificationSettingsDto updateNotificationSetting(@NonNull String userId, @NonNull NotificationSettingsDto settingsDto) {
        Collection<NotificationSettings> existingOrDefault = getSettingsOrDefault(userId);
        // find setting for type
        NotificationSettings toUpdate = existingOrDefault.stream()
            .filter(ns -> ns.getType() == settingsDto.getType())
            .findFirst().get();
        // update setting
        toUpdate.setChannels(
            settingsDto.getChannels().isEmpty()
                ? EnumSet.noneOf(NotificationChannel.class) // NONE
                : EnumSet.copyOf(settingsDto.getChannels())
        );
        toUpdate = notificationSettingsDao.save(toUpdate);

        publisher.publishEvent(NotificationSettingsEvent.settingsUpdated(userId, toUpdate));
        return NotificationSettingsMapper.toDto(toUpdate);
    }

    private Collection<NotificationSettings> getSettingsOrDefault(String userId) {
        var user = volunteerDao.getById(userId);
        Collection<NotificationSettings> existingSettings = notificationSettingsDao.findAllByUserId(userId);

        // create map for lookup
        Map<NotificationType, NotificationSettings> settingsByType = existingSettings.stream()
            .collect(Collectors.toMap(NotificationSettings::getType, ns -> ns));

        // return saved settings if present, else default ones
        return Arrays.stream(NotificationType.values())
            .map(type -> {
                NotificationSettings existingSetting = settingsByType.get(type);
                if (existingSetting != null) {
                    return existingSetting;
                } else {
                    return NotificationSettings.builder()
                        .type(type)
                        .channels(EnumSet.of(NotificationChannel.PUSH)) // default channels
                        .user(user)
                        .build();
                }
            }).toList();
    }
}
