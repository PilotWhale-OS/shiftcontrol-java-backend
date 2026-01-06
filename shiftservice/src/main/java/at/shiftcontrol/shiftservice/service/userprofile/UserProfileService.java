package at.shiftcontrol.shiftservice.service.userprofile;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;

public interface UserProfileService {
    UserProfileDto getUserProfile(String userId) throws NotFoundException, ForbiddenException;
}
