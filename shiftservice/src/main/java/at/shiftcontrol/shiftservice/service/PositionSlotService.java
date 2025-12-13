package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.PositionSlotDto;

public interface PositionSlotService {
    PositionSlotDto findById(Long id) throws NotFoundException;

    AssignmentDto join(Long positionSlotId, Long userId) throws NotFoundException;

    void leave(Long positionSlotId, Long userId);

    Collection<AssignmentDto> getAssignments(Long positionSlotId);
}
