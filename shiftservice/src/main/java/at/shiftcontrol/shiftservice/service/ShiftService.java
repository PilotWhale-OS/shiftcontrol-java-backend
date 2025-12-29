package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDetailsDto;

public interface ShiftService {
    ShiftDetailsDto getShiftDetails(long shiftId, String userId) throws NotFoundException;
}
