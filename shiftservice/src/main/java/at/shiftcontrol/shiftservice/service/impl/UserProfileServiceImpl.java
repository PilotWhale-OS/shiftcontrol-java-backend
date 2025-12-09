package at.shiftcontrol.shiftservice.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.AccountInfoDto;
import at.shiftcontrol.shiftservice.dto.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.dto.UserProfileDto;
import at.shiftcontrol.shiftservice.mapper.RoleMapper;
import at.shiftcontrol.shiftservice.service.UserProfileService;
import at.shiftcontrol.shiftservice.type.NotificationChannel;
import at.shiftcontrol.shiftservice.type.NotificationType;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final VolunteerDao dao;

    @Override
    public UserProfileDto getUserProfile(Long userId) throws NotFoundException {
        var volunteer = dao.findById(userId).orElseThrow(() -> new NotFoundException("Volunteer not found"));

        var accountInfo = new AccountInfoDto(String.valueOf(volunteer.getId()), volunteer.getUsername(), volunteer.getEmail());

        var notificationSettings = new HashMap<NotificationType, Set<NotificationChannel>>();
        volunteer.getNotificationAssignments().forEach(assignment -> {
            var current = notificationSettings.computeIfAbsent(assignment.getNotificationType(), (ignored) -> new HashSet<>());
            current.add(assignment.getNotificationChannel());
            notificationSettings.put(assignment.getNotificationType(), current);
        });

        return new UserProfileDto(
            accountInfo,
            new NotificationSettingsDto(notificationSettings),
            RoleMapper.toRoleDto(volunteer.getRoles())
        );
    }

    //     public NotificationSettingsDto updateNotificationSetting(NotificationSettingsDto settingsDto){
    //             var volunteer = dao.findById(setting.getUserId()).orElseThrow(() -> new NotFoundException("Volunteer not found"));
    //             var currentNotifications = volunteer.getNotificationAssignments();
    //         settingsDto.getPerTypeSettings().forEach(setting -> {
    //             if (currentNotifications.contains(setting)) {
    //
    //             }
    //
    //             dao.save(volunteer);
    //         });
    //
    //
    //         return RoleMapper.toNotificationSettingsDto()
    //     }
}
