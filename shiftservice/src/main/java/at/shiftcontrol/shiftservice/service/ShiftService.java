package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.ShiftDetailsDto;

public interface ShiftService {
    ShiftDetailsDto getShiftDetails(long shiftId, long userId) throws NotFoundException;
}
