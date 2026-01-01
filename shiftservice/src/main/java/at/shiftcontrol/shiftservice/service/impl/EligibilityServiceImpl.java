package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.Authorities;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.PositionSlotJoinErrorDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import at.shiftcontrol.shiftservice.type.PositionSignupState;
import at.shiftcontrol.shiftservice.type.TradeStatus;

@Service
@RequiredArgsConstructor
public class EligibilityServiceImpl implements EligibilityService {
    private final VolunteerDao volunteerDao;
    private final PositionSlotDao positionSlotDao;
    private final AssignmentDao assignmentDao;

    private final ApplicationUserProvider userProvider;

    @Override
    public PositionSignupState getSignupStateForPositionSlot(Long positionSlotId, String userId) throws NotFoundException {
        return getSignupStateForPositionSlot(
            positionSlotDao.findById(positionSlotId).orElseThrow(() -> new NotFoundException("PositionSlot with ID %d not found".formatted(positionSlotId))),
            volunteerDao.findByUserId(userId).orElseThrow(() -> new NotFoundException("Volunteer with ID %s not found".formatted(userId))
            ));
    }

    @Override
    public PositionSignupState getSignupStateForPositionSlot(PositionSlot positionSlot, String userId) throws NotFoundException {
        return getSignupStateForPositionSlot(
            positionSlot,
            volunteerDao.findByUserId(userId).orElseThrow(() -> new NotFoundException("Volunteer with ID %s not found".formatted(userId))
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

        if (hasAuction(positionSlot)) {
            return PositionSignupState.SIGNUP_VIA_AUCTION;
        }

        boolean hasCapacity = positionSlot.getAssignments().size() < positionSlot.getDesiredVolunteerCount();
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
    public void validateSignUpStateForJoin(PositionSlot positionSlot, Volunteer volunteer) throws ConflictException {
        PositionSignupState signupState = this.getSignupStateForPositionSlot(positionSlot, volunteer);
        switch (signupState) {
            case SIGNED_UP, FULL, SIGNUP_VIA_TRADE, SIGNUP_VIA_AUCTION:
                // simply joining is not possible
                throw new ConflictException(PositionSlotJoinErrorDto.builder().state(signupState).build());
            case NOT_ELIGIBLE:
                if (!userProvider.currentUserHasAuthority(Authorities.CAN_JOIN_UNELIGIBLE_POSITIONS)) {
                    throw new ConflictException(PositionSlotJoinErrorDto.builder().state(signupState).build());
                }
                break;
            case SIGNUP_POSSIBLE, SIGNUP_OR_TRADE:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + signupState);
        }
    }

    @Override
    public void validateSignUpStateForAuction(PositionSlot positionSlot, Volunteer volunteer) throws ConflictException {
        PositionSignupState signupState = this.getSignupStateForPositionSlot(positionSlot, volunteer);
        switch (signupState) {
            case SIGNED_UP, FULL, SIGNUP_VIA_TRADE, SIGNUP_POSSIBLE, SIGNUP_OR_TRADE:
                throw new ConflictException(PositionSlotJoinErrorDto.builder().state(signupState).build());
            case NOT_ELIGIBLE:
                if (!userProvider.currentUserHasAuthority(Authorities.CAN_JOIN_UNELIGIBLE_POSITIONS)) {
                    throw new ConflictException(PositionSlotJoinErrorDto.builder().state(signupState).build());
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
    public boolean isTradePossible(PositionSlot positionSlot, Volunteer volunteer) {
        PositionSignupState signupState = this.getSignupStateForPositionSlot(positionSlot, volunteer);
        return !PositionSignupState.SIGNED_UP.equals(signupState)
            && !PositionSignupState.NOT_ELIGIBLE.equals(signupState);
    }

    @Override
    public void validateIsTradePossible(PositionSlot positionSlot, Volunteer volunteer) throws ConflictException {
        if (!isTradePossible(positionSlot, volunteer)) {
            throw new ConflictException("position slot can not be traded with volunteer");
        }
    }

    @Override
    public Collection<Assignment> getConflictingAssignments(String volunteerId, Instant startTime, Instant endTime) {
        return assignmentDao.getConflictingAssignments(volunteerId, startTime, endTime);
    }

    @Override
    public void validateHasConflictingAssignments(String volunteerId, Instant startTime, Instant endTime) throws ConflictException {
        var a = assignmentDao.getConflictingAssignments(volunteerId, startTime, endTime);
        if (a.isEmpty()) return;
        throw new ConflictException("User has conflicting assignments");
    }

    @Override
    public Collection<Assignment> getConflictingAssignmentsExcludingSlot(String volunteerId, Instant startTime, Instant endTime, long positionSlot) {
        return assignmentDao.getConflictingAssignmentsExcludingSlot(volunteerId, startTime, endTime, positionSlot);
    }

    @Override
    public void validateHasConflictingAssignmentsExcludingSlot(String volunteerId, Instant startTime, Instant endTime, long positionSlot)
        throws ConflictException {
        var a = assignmentDao.getConflictingAssignmentsExcludingSlot(volunteerId, startTime, endTime, positionSlot);
        if (a.isEmpty()) return;
        throw new ConflictException("User has conflicting assignments");
    }

    private boolean isSignedUp(PositionSlot positionSlot, Volunteer volunteer) {
        for (var assignment : positionSlot.getAssignments()) {
            if (assignment.getAssignedVolunteer().getId().equals(volunteer.getId())) {
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
    private boolean hasOpenTradeForUser(PositionSlot slot, String userId) {
        return slot.getAssignments().stream().anyMatch(assignment ->
            assignment.getOutgoingSwitchRequests().stream().anyMatch(req ->
                req.getStatus() == TradeStatus.OPEN
                    && req.getRequestedAssignment().getAssignedVolunteer().getId().equals(userId)
            )
        );
    }
}
