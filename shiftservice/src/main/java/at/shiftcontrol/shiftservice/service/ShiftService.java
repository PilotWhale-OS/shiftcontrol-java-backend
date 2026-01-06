package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.shiftservice.dto.shift.ShiftDetailsDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftModificationDto;

public interface ShiftService {
    ShiftDetailsDto getShiftDetails(long shiftId, String userId);

    ShiftDto createShift(long shiftPlanId, ShiftModificationDto modificationDto);

    ShiftDto updateShift(long shiftId, ShiftModificationDto modificationDto);

    void deleteShift(long shiftId);
}
