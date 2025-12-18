package at.shiftcontrol.shiftservice.dao.UserProfile;

import java.util.Optional;

import at.shiftcontrol.shiftservice.dto.UserProfile.UserProfileDto;

public interface UserProfileDao {
    Optional<UserProfileDto> findSettingsByUserId(Long userId);
}
