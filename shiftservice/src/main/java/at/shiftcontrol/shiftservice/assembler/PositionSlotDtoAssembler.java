package at.shiftcontrol.shiftservice.assembler;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.mapper.PositionSlotMapper;
import at.shiftcontrol.shiftservice.service.EligibilityService;

@RequiredArgsConstructor
@Service
public class PositionSlotDtoAssembler {

    private final EligibilityService eligibilityService;

    public PositionSlotDto assemble(@NonNull PositionSlot positionSlot) {
        // TODO get current User
        Volunteer volunteer = Volunteer.builder().id(1L).build();
        // calculates SignupState for current user and
        // gets all trade offers for this slot for the current user
        return PositionSlotMapper.toDto(positionSlot,
            eligibilityService.getSignupStateForPositionSlot(positionSlot, volunteer),
            filterTradesForUser(positionSlot.getAssignments(), volunteer.getId())
        );
    }

    public Collection<PositionSlotDto> assemble(@NonNull Collection<PositionSlot> positionSlots) {
        return positionSlots.stream().map(this::assemble).toList();
    }

    private Collection<AssignmentSwitchRequest> filterTradesForUser(Collection<Assignment> assignments, long userId) {
        return assignments.stream()
            .filter(assignment -> assignment.getAssignedVolunteer().getId() == userId)
            .flatMap(assignment -> assignment.getIncomingSwitchRequests().stream())
            .toList();
    }
}
