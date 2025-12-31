package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.Collections;

import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.type.PositionSignupState;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PositionSlotAssemblingMapper {
    private final EligibilityService eligibilityService;
    private final ApplicationUserProvider applicationUserProvider;
    private final VolunteerDao volunteerDao;
    private final PositionSlotDao positionSlotDao;

    public PositionSlotDto assemble(@NonNull PositionSlot positionSlot) {
        var volunteer = volunteerDao.findByUserId(applicationUserProvider.getCurrentUser().getUserId())
            .orElseThrow(() -> new IllegalStateException("Current user has no volunteer entity"));
        var preferenceValue = positionSlotDao.getPreference(volunteer.getId(), positionSlot.getId());

        // calculates SignupState for current user and
        // gets all trade offers for this slot for the current user
        return toDto(positionSlot,
            eligibilityService.getSignupStateForPositionSlot(positionSlot, volunteer),
            filterTradesForUser(positionSlot.getAssignments(), volunteer.getId()),
            preferenceValue);
    }

    public Collection<PositionSlotDto> assemble(@NonNull Collection<PositionSlot> positionSlots) {
        return positionSlots.stream().map(this::assemble).toList();
    }

    private Collection<AssignmentSwitchRequest> filterTradesForUser(Collection<Assignment> assignments, String userId) {
        if (assignments == null || assignments.isEmpty()) {
            return Collections.emptyList();
        }

        return assignments.stream()
            .filter(assignment -> assignment.getAssignedVolunteer().getId().equals(userId))
            .flatMap(assignment -> assignment.getIncomingSwitchRequests().stream())
            .toList();
    }

    public static PositionSlotDto toDto(@NonNull PositionSlot positionSlot, @NonNull PositionSignupState positionSignupState,
                                        Collection<AssignmentSwitchRequest> tradesForUser, int preferenceValue) {
        var assignments = positionSlot.getAssignments();
        Collection<VolunteerDto> assignedVolunteers;
        Collection<AssignmentDto> assignmentsDtos;

        if (assignments == null) {
            assignedVolunteers = null;
            assignmentsDtos = null;
        } else {
            var volunteers = assignments.stream().map(Assignment::getAssignedVolunteer).toList();
            assignedVolunteers = VolunteerMapper.toDto(volunteers);

            assignmentsDtos = AssignmentMapper.toAuctionDto(assignments); // get open auctions for this slot
        }

        return new PositionSlotDto(
            String.valueOf(positionSlot.getId()),
            positionSlot.getName(),
            positionSlot.getDescription(),
            positionSlot.isSkipAutoAssignment(),
            String.valueOf(positionSlot.getShift().getId()),
            RoleMapper.toRoleDto(positionSlot.getRole()),
            assignedVolunteers,
            positionSlot.getDesiredVolunteerCount(),
            positionSlot.getRewardPoints(),
            positionSignupState,
            TradeMapper.toTradeInfoDto(tradesForUser),
            assignmentsDtos,
            preferenceValue);
    }
}
