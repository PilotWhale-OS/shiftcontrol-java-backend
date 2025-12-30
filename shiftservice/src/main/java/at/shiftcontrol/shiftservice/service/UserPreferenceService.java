package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.shiftservice.dto.PositionSlotPreferenceDto;

public interface UserPreferenceService {
    PositionSlotPreferenceDto getUserPreference(String userId, long shiftId);
}
