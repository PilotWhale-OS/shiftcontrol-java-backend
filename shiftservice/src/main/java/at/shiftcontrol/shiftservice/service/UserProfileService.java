package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;

public interface UserProfileService {
    UserProfileDto getUserProfile(String userId) throws NotFoundException;
}
