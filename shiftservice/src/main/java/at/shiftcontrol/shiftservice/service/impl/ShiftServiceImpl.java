package at.shiftcontrol.shiftservice.service.impl;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDetailsDto;
import at.shiftcontrol.shiftservice.mapper.ShiftAssemblingMapper;
import at.shiftcontrol.shiftservice.service.ShiftService;
import at.shiftcontrol.shiftservice.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {
    private final ShiftDao shiftDao;
    private final UserPreferenceService userPreferenceService;
    private final ShiftAssemblingMapper shiftAssemblingMapper;

    @Override
    public ShiftDetailsDto getShiftDetails(long shiftId, String userId) throws NotFoundException {
        var shift = shiftDao.findById(shiftId).orElseThrow(() -> new NotFoundException("Shift not found"));
        var userPref = userPreferenceService.getUserPreference(userId, shiftId);

        return ShiftDetailsDto.builder()
            .shift(shiftAssemblingMapper.assemble(shift))
            .preference(userPref)
            .build();
    }
}
