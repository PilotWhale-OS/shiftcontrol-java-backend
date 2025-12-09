package at.shiftcontrol.shiftservice.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.AccountInfoDto;
import at.shiftcontrol.shiftservice.dto.UserProfileDto;
import at.shiftcontrol.shiftservice.mapper.RoleMapper;
import at.shiftcontrol.shiftservice.service.UserProfileService;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final VolunteerDao dao;

    @Override
    public UserProfileDto getUserProfile(Long userId) throws NotFoundException {
        var volunteer = dao.findById(userId).orElseThrow(() -> new NotFoundException("Volunteer not found"));

        var accountInfo = new AccountInfoDto(String.valueOf(volunteer.getId()), volunteer.getUsername(), volunteer.getEmail());

        var notificationSettingsDto

        return new UserProfileDto(
            accountInfo,
            volunteer.getNotificationAssignments(),
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
