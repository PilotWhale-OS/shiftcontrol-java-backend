package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.shiftservice.dto.UserShiftPreferenceDto;

public interface UserPreferenceService {
    UserShiftPreferenceDto getUserPreference(String userId, long shiftId);
}
