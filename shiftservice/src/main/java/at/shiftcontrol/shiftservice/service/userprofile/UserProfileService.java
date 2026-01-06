package at.shiftcontrol.shiftservice.service.userprofile;

import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;

public interface UserProfileService {
    UserProfileDto getUserProfile(String userId);
}
