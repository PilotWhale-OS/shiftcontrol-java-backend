package at.shiftcontrol.shiftservice.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dto.PositionSlotPreferenceDto;
import at.shiftcontrol.shiftservice.service.UserPreferenceService;

@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {
    @Override
    public PositionSlotPreferenceDto getUserPreference(String userId, long shiftId) {
        //TODO implement method
        return null;
    }
}
