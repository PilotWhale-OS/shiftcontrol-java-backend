package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.dto.PaginationDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDetailsDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftModificationDto;

public interface ShiftService {
    ShiftDetailsDto getShiftDetails(long shiftId, String userId);

    PaginationDto<ShiftDto> getAllOpenShiftsOfPlanPaginated(long planId, int page, int size);

    ShiftDto createShift(long shiftPlanId, ShiftModificationDto modificationDto);

    ShiftDto updateShift(long shiftId, ShiftModificationDto modificationDto);

    void deleteShift(long shiftId);
}
