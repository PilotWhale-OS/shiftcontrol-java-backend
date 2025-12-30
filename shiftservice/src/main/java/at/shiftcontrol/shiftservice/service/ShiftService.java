package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDetailsDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftModificationDto;

public interface ShiftService {
    ShiftDetailsDto getShiftDetails(long shiftId, String userId) throws NotFoundException;

    ShiftDto createShift(long shiftPlanId, ShiftModificationDto modificationDto) throws NotFoundException;

    ShiftDto updateShift(long shiftId, ShiftModificationDto modificationDto) throws NotFoundException;

    void deleteShift(long shiftId) throws NotFoundException;
}
