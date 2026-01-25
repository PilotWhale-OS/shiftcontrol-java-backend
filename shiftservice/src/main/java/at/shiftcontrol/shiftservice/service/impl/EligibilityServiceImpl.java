package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.IllegalStateException;
import at.shiftcontrol.lib.type.AssignmentStatus;
import at.shiftcontrol.lib.type.PositionSignupState;
import at.shiftcontrol.lib.type.TradeStatus;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.Authorities;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.TimeConstraintDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotJoinErrorDto;
import at.shiftcontrol.shiftservice.service.EligibilityService;

@Service
@RequiredArgsConstructor
public class EligibilityServiceImpl implements EligibilityService {
    private final VolunteerDao volunteerDao;
    private final PositionSlotDao positionSlotDao;
    private final AssignmentDao assignmentDao;
    private final TimeConstraintDao timeConstraintDao;

    private final ApplicationUserProvider userProvider;

    @Override
    public PositionSignupState getSignupStateForPositionSlot(Long positionSlotId, String userId) {
        return getSignupStateForPositionSlot(
            positionSlotDao.getById(positionSlotId),
            volunteerDao.getById(userId)
            );
    }

    @Override
    public PositionSignupState getSignupStateForPositionSlot(PositionSlot positionSlot, String userId) {
        return getSignupStateForPositionSlot(
            positionSlot,
            volunteerDao.getById(userId)
            );
    }

    @Override
    public PositionSignupState getSignupStateForPositionSlot(PositionSlot positionSlot, Volunteer volunteer) {
        if (isSignedUp(positionSlot, volunteer)) {
            return PositionSignupState.SIGNED_UP;
        }

        boolean eligibleForRole = positionSlot.getRole() == null || (volunteer.getRoles() != null && volunteer.getRoles().contains(positionSlot.getRole()));
        if (!eligibleForRole) {
            return PositionSignupState.NOT_ELIGIBLE;
        }

        var conflictingConstraints =
            timeConstraintDao.findByPositionSlotIdAndVolunteerId(positionSlot.getId(), volunteer.getId());
        if (!conflictingConstraints.isEmpty()) {
            return PositionSignupState.TIME_CONFLICT_TIME_CONSTRAINT;
        }

        var conflictingAssignments = assignmentDao.getConflictingAssignmentsExcludingSlot(
            volunteer.getId(),
            positionSlot.getShift().getStartTime(),
            positionSlot.getShift().getEndTime(),
            positionSlot.getId()
        );
        if (!conflictingAssignments.isEmpty()) {
            return PositionSignupState.TIME_CONFLICT_ASSIGNMENT;
        }

        //Positive cases
        if (hasAuction(positionSlot)) {
            return PositionSignupState.SIGNUP_VIA_AUCTION;
        }

        boolean hasCapacity = hasCapacity(positionSlot);
        boolean isTradePossible = hasOpenTradeForUser(positionSlot, volunteer.getId());

        if (hasCapacity && isTradePossible) {
            return PositionSignupState.SIGNUP_OR_TRADE;
        }

        if (hasCapacity) {
            return PositionSignupState.SIGNUP_POSSIBLE;
        }

        // Slot is full: check alternative mechanisms
        if (isTradePossible) {
            return PositionSignupState.SIGNUP_VIA_TRADE;
        }

        // No special mechanisms, just full
        return PositionSignupState.FULL;
    }

    @Override
    public void validateSignUpStateForJoin(PositionSlot positionSlot, Volunteer volunteer) {
        PositionSignupState signupState = this.getSignupStateForPositionSlot(positionSlot, volunteer);
        switch (signupState) {
            case SIGNED_UP, FULL, SIGNUP_VIA_TRADE, SIGNUP_VIA_AUCTION:
                // simply joining is not possible
                throw new ConflictException(new PositionSlotJoinErrorDto(signupState));
            case NOT_ELIGIBLE, TIME_CONFLICT_ASSIGNMENT, TIME_CONFLICT_TIME_CONSTRAINT:
                if (!userProvider.currentUserHasAuthority(Authorities.CAN_JOIN_UNELIGIBLE_POSITIONS)) {
                    throw new ConflictException(new PositionSlotJoinErrorDto(signupState));
                }
                break;
            case SIGNUP_POSSIBLE, SIGNUP_OR_TRADE:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + signupState);
        }
    }

    @Override
    public void validateSignUpStateForAuction(PositionSlot positionSlot, Volunteer volunteer) {
        PositionSignupState signupState = this.getSignupStateForPositionSlot(positionSlot, volunteer);
        switch (signupState) {
            case SIGNED_UP, FULL, SIGNUP_VIA_TRADE, SIGNUP_POSSIBLE, SIGNUP_OR_TRADE:
                throw new ConflictException(new PositionSlotJoinErrorDto(signupState));
            case NOT_ELIGIBLE:
                if (!userProvider.currentUserHasAuthority(Authorities.CAN_JOIN_UNELIGIBLE_POSITIONS)) {
                    throw new ConflictException(new PositionSlotJoinErrorDto(signupState));
                }
                break;
            case SIGNUP_VIA_AUCTION:
                // only possible status for claiming an auction
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + signupState);
        }
    }

    @Override
    public boolean isEligibleAndNotSignedUp(PositionSlot positionSlot, Volunteer volunteer) {
        PositionSignupState signupState = this.getSignupStateForPositionSlot(positionSlot, volunteer);
        return !PositionSignupState.SIGNED_UP.equals(signupState)
            && !PositionSignupState.NOT_ELIGIBLE.equals(signupState);
    }

    @Override
    public void validateIsEligibleAndNotSignedUp(PositionSlot positionSlot, Volunteer volunteer) {
        if (!isEligibleAndNotSignedUp(positionSlot, volunteer)) {
            throw new ConflictException("position slot can not be assigned to volunteer");
        }
    }

    @Override
    public Collection<Assignment> getConflictingAssignments(String volunteerId, Instant startTime, Instant endTime) {
        return assignmentDao.getConflictingAssignments(volunteerId, startTime, endTime);
    }

    @Override
    public Collection<Assignment> getConflictingAssignments(String volunteerId, PositionSlot positionSlot) {
        return getConflictingAssignments(volunteerId, positionSlot.getShift().getStartTime(), positionSlot.getShift().getEndTime());
    }

    @Override
    public Collection<Assignment> getConflictingAssignmentsExcludingSlot(String volunteerId, Instant startTime, Instant endTime, long positionSlot) {
        return assignmentDao.getConflictingAssignmentsExcludingSlot(volunteerId, startTime, endTime, positionSlot);
    }

    @Override
    public Collection<Assignment> getConflictingAssignmentsExcludingSlot(String volunteerId, PositionSlot positionSlot, long slotToExclude) {
        return getConflictingAssignmentsExcludingSlot(
            volunteerId, positionSlot.getShift().getStartTime(), positionSlot.getShift().getEndTime(), slotToExclude);
    }

    @Override
    public void validateHasConflictingAssignmentsExcludingSlot(String volunteerId, Instant startTime, Instant endTime, long positionSlot) {
        var a = assignmentDao.getConflictingAssignmentsExcludingSlot(volunteerId, startTime, endTime, positionSlot);
        if (a.isEmpty()) {
            return;
        }
        throw new ConflictException("User has conflicting assignments");
    }

    @Override
    public void validateHasConflictingAssignmentsExcludingSlot(String volunteerId, PositionSlot positionSlot, long slotToExclude) {
        validateHasConflictingAssignmentsExcludingSlot(
            volunteerId, positionSlot.getShift().getStartTime(), positionSlot.getShift().getEndTime(), slotToExclude);
    }

    @Override
    public boolean hasCapacity(PositionSlot positionSlot) {
        if (positionSlot.getAssignments() != null) {
            return positionSlot.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REQUEST_FOR_ASSIGNMENT).toList()
                .size() < positionSlot.getDesiredVolunteerCount();
        }
        return true;
    }

    private boolean isSignedUp(PositionSlot positionSlot, Volunteer volunteer) {
        if (positionSlot.getAssignments() == null) {
            return false;
        }

        for (var assignment : positionSlot.getAssignments()) {
            if (assignment.getAssignedVolunteer() != null && assignment.getAssignedVolunteer().getId().equals(volunteer.getId())) {
                var status = assignment.getStatus();
                return status != AssignmentStatus.REQUEST_FOR_ASSIGNMENT; // pending requests do not count as signed up
            }
        }
        return false;
    }

    private boolean hasAuction(PositionSlot slot) {
        return slot.getAssignments() != null && slot.getAssignments().stream()
            .anyMatch(a -> a.getStatus() == AssignmentStatus.AUCTION || a.getStatus() == AssignmentStatus.AUCTION_REQUEST_FOR_UNASSIGN);
    }

    // trade requests where the requested assignment is assigned to the user
    private boolean hasOpenTradeForUser(PositionSlot slot, String userId) {
        return slot.getAssignments() != null && slot.getAssignments().stream().anyMatch(assignment ->
            assignment.getOutgoingSwitchRequests().stream().anyMatch(req ->
                req.getStatus() == TradeStatus.OPEN
                    && req.getRequestedAssignment().getAssignedVolunteer().getId().equals(userId)
            )
        );
    }
}
