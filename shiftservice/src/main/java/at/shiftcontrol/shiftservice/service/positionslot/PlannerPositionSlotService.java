package at.shiftcontrol.shiftservice.service.positionslot;

import java.util.Collection;

import jakarta.validation.Valid;

import at.shiftcontrol.shiftservice.dto.AssignmentAssignDto;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentFilterDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentRequestDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;

public interface PlannerPositionSlotService {
    Collection<AssignmentRequestDto> getSlots(long shiftPlanId, @Valid AssignmentFilterDto filterDto);

    void acceptRequest(long shiftPlanId, long positionSlotId, String userId);

    void declineRequest(long shiftPlanId, long positionSlotId, String userId);

    Collection<VolunteerDto> getAssignableUsers(String positionSlotId);

    Collection<AssignmentDto> assignUsersToSlot(AssignmentAssignDto assignmentAssignDto);
}
