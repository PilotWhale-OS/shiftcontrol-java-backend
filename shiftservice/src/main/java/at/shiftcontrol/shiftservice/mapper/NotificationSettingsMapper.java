package at.shiftcontrol.shiftservice.mapper;

import lombok.NoArgsConstructor;

import at.shiftcontrol.lib.entity.NotificationSettings;
import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class NotificationSettingsMapper {
    public static NotificationSettings toEntity(NotificationSettingsDto dto) {
        return NotificationSettings.builder()
            .channels(dto.getChannels())
            .type(dto.getType())
            .build();
    }
}
