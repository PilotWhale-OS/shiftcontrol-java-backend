package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotPreferenceDto;

public interface UserPreferenceService {
    PositionSlotPreferenceDto getUserPreference(String userId, long shiftId);
}
