package at.shiftcontrol.shiftservice.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dto.UserShiftPreferenceDto;
import at.shiftcontrol.shiftservice.service.UserPreferenceService;

@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {
    @Override
    public UserShiftPreferenceDto getUserPreference(String userId, long shiftId) {
        //TODO implement method
        return null;
    }
}
