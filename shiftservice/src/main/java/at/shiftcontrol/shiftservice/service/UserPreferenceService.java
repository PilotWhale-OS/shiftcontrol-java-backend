package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.shiftservice.dto.UserShiftPreferenceDto;

public interface UserPreferenceService {
    UserShiftPreferenceDto getUserPreference(long userId, long shiftId);
}
