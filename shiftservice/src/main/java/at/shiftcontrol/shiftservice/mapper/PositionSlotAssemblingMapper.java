package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.type.PositionSignupState;

@RequiredArgsConstructor
@Service
public class PositionSlotAssemblingMapper {
    private final EligibilityService eligibilityService;
    private final ApplicationUserProvider applicationUserProvider;
    private final VolunteerDao volunteerDao;

    public PositionSlotDto assemble(@NonNull PositionSlot positionSlot) {
        var volunteer = volunteerDao.findByUserId(applicationUserProvider.getCurrentUser().getUserId())
            .orElseThrow(() -> new IllegalStateException("Current user has no volunteer entity"));

        // calculates SignupState for current user and
        // gets all trade offers for this slot for the current user
        return toDto(positionSlot,
            eligibilityService.getSignupStateForPositionSlot(positionSlot, volunteer),
            filterTradesForUser(positionSlot.getAssignments(), volunteer.getId())
        );
    }

    public Collection<PositionSlotDto> assemble(@NonNull Collection<PositionSlot> positionSlots) {
        return positionSlots.stream().map(this::assemble).toList();
    }

    private Collection<AssignmentSwitchRequest> filterTradesForUser(Collection<Assignment> assignments, String userId) {
        return assignments.stream()
            .filter(assignment -> assignment.getAssignedVolunteer().getId().equals(userId))
            .flatMap(assignment -> assignment.getIncomingSwitchRequests().stream())
            .toList();
    }

    public static PositionSlotDto toDto(@NonNull PositionSlot positionSlot, @NonNull PositionSignupState positionSignupState,
                                        Collection<AssignmentSwitchRequest> tradesForUser) {
        var volunteers = positionSlot.getAssignments().stream().map(Assignment::getAssignedVolunteer).toList();

        return new PositionSlotDto(
            String.valueOf(positionSlot.getId()),
            String.valueOf(positionSlot.getShift().getId()),
            RoleMapper.toRoleDto(positionSlot.getRole()),
            VolunteerMapper.toDto(volunteers),
            positionSlot.getDesiredVolunteerCount(),
            positionSlot.getRewardPoints(),
            positionSignupState,
            TradeMapper.toTradeInfoDto(tradesForUser),
            AssignmentMapper.toAuctionDto(positionSlot.getAssignments())); // get open auctions for this slot
    }
}
