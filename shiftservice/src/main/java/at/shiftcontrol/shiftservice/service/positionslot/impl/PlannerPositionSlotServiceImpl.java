package at.shiftcontrol.shiftservice.service.positionslot.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentFilterDto;
import at.shiftcontrol.shiftservice.service.positionslot.PlannerPositionSlotService;

@Service
@RequiredArgsConstructor
public class PlannerPositionSlotServiceImpl implements PlannerPositionSlotService {
    @Override
    public Collection<PositionSlotDto> getSlots(long shiftPlanId, AssignmentFilterDto filterDto) {
        return List.of();
    }

    @Override
    public void acceptRequest(long shiftPlanId, long positionSlotId) {
    }

    @Override
    public void declineRequest(long shiftPlanId, long positionSlotId) {
    }
}
