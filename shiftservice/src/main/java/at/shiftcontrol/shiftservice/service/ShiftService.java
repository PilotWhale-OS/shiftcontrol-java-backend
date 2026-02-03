package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.dto.PaginationDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDetailsDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftModificationDto;

import lombok.NonNull;

public interface ShiftService {
    @NonNull ShiftDetailsDto getShiftDetails(long shiftId, @NonNull String userId);

    @NonNull PaginationDto<ShiftDto> getAllOpenShiftsOfPlanPaginated(long planId, int page, int size);

    @NonNull ShiftDto createShift(long shiftPlanId, @NonNull ShiftModificationDto modificationDto);

    @NonNull ShiftDto updateShift(long shiftId, @NonNull ShiftModificationDto modificationDto);

    void deleteShift(long shiftId);
}
