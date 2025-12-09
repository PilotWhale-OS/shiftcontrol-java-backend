package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.UserProfileDto;

public interface UserProfileService {
    UserProfileDto getUserProfile(Long userId) throws NotFoundException;
}
