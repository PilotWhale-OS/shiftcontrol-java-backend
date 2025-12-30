package at.shiftcontrol.shiftservice.service.positionslot;

import java.util.Collection;

import jakarta.validation.Valid;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentFilterDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentRequestDto;

public interface PlannerPositionSlotService {
    Collection<AssignmentRequestDto> getSlots(long shiftPlanId, @Valid AssignmentFilterDto filterDto) throws ForbiddenException, NotFoundException;

    void acceptRequest(long shiftPlanId, long positionSlotId, String userId) throws ForbiddenException;

    void declineRequest(long shiftPlanId, long positionSlotId, String userId) throws ForbiddenException;
}
