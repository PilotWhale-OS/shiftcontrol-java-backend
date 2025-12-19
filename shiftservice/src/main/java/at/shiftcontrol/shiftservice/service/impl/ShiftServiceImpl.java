package at.shiftcontrol.shiftservice.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dto.ShiftDetailsDto;
import at.shiftcontrol.shiftservice.mapper.ShiftMapper;
import at.shiftcontrol.shiftservice.service.ShiftService;
import at.shiftcontrol.shiftservice.service.UserPreferenceService;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {
    private final ShiftDao shiftDao;
    private final UserPreferenceService userPreferenceService;

    @Override
    public ShiftDetailsDto getShiftDetails(long shiftId, String userId) throws NotFoundException {
        var shift = shiftDao.findById(shiftId).orElseThrow(() -> new NotFoundException("Shift not found"));
        var userPref = userPreferenceService.getUserPreference(userId, shiftId);

        return ShiftDetailsDto.builder()
            .shift(ShiftMapper.toShiftDto(shift))
            .preference(userPref)
            .build();
    }
}
