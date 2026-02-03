package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotPreferenceDto;

import lombok.NonNull;

public interface UserPreferenceService {
    @NonNull
    PositionSlotPreferenceDto getUserPreference(@NonNull String userId, long shiftId);
}
