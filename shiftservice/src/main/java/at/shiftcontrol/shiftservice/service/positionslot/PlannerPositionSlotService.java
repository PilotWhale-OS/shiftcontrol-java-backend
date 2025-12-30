package at.shiftcontrol.shiftservice.service.positionslot;

import java.util.Collection;

import jakarta.validation.Valid;

import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentFilterDto;

public interface PlannerPositionSlotService {
    Collection<PositionSlotDto> getSlots(long shiftPlanId, @Valid AssignmentFilterDto filterDto);

    void acceptRequest(long shiftPlanId, long positionSlotId);

    void declineRequest(long shiftPlanId, long positionSlotId);
}
