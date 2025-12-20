package at.shiftcontrol.shiftservice.dao.userprofile;

import java.util.Optional;

import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;

@Deprecated
public interface UserProfileDao {
    Optional<UserProfileDto> findSettingsByUserId(Long userId);
}
