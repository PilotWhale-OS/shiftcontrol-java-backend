package at.shiftcontrol.shiftservice.service.impl;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.VolunteerDao;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import at.shiftcontrol.shiftservice.type.PositionSignupState;
import at.shiftcontrol.shiftservice.type.TradeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EligibilityServiceImpl implements EligibilityService {
    private final VolunteerDao volunteerDao;
    private final PositionSlotDao positionSlotDao;

    @Override
    public PositionSignupState getSignupStateForPositionSlot(Long positionSlotId, Long userId) throws NotFoundException {
        return getSignupStateForPositionSlot(
            positionSlotDao.findById(positionSlotId).orElseThrow(() -> new NotFoundException("PositionSlot with ID %d not found".formatted(positionSlotId))),
            volunteerDao.findByUserId(userId).orElseThrow(() -> new NotFoundException("Volunteer with ID %d not found".formatted(userId))
            ));
    }

    @Override
    public PositionSignupState getSignupStateForPositionSlot(PositionSlot positionSlot, Long userId) throws NotFoundException {
        return getSignupStateForPositionSlot(
            positionSlot,
            volunteerDao.findByUserId(userId).orElseThrow(() -> new NotFoundException("Volunteer with ID %d not found".formatted(userId))
            ));
    }

    @Override
    public PositionSignupState getSignupStateForPositionSlot(PositionSlot positionSlot, Volunteer volunteer) {
        if (isSignedUp(positionSlot, volunteer)) {
            return PositionSignupState.SIGNED_UP;
        }

        boolean eligibleForRole = volunteer.getRoles().contains(positionSlot.getRole());
        if (!eligibleForRole) {
            return PositionSignupState.NOT_ELIGIBLE;
        }

        boolean hasCapacity = positionSlot.getAssignments().size() < positionSlot.getDesiredVolunteerCount();
        if (hasCapacity) {
            return PositionSignupState.SIGNUP_POSSIBLE;
        }

        // Slot is full: check alternative mechanisms
        if (hasOpenTradeForUser(positionSlot, volunteer.getId())) {
            return PositionSignupState.SIGNUP_VIA_TRADE;
        }

        if (hasAuction(positionSlot)) {
            return PositionSignupState.SIGNUP_VIA_AUCTION;
        }

        // No special mechanisms, just full
        return PositionSignupState.FULL;
    }

    private boolean isSignedUp(PositionSlot positionSlot, Volunteer volunteer) {
        for (var assignment : positionSlot.getAssignments()) {
            if (assignment.getAssignedVolunteer().getId() == volunteer.getId()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAuction(PositionSlot slot) {
        return slot.getAssignments().stream()
            .anyMatch(a -> a.getStatus() == AssignmentStatus.AUCTION);
    }

    // trade requests where the requested assignment is assigned to the user
    private boolean hasOpenTradeForUser(PositionSlot slot, long userId) {
        return slot.getAssignments().stream().anyMatch(assignment ->
            assignment.getOutgoingSwitchRequests().stream().anyMatch(req ->
                req.getStatus() == TradeStatus.OPEN
                    && req.getRequestedAssignment() != null
                    && req.getRequestedAssignment().getAssignedVolunteer() != null
                    && req.getRequestedAssignment().getAssignedVolunteer().getId() == userId
            )
        );
    }
}
