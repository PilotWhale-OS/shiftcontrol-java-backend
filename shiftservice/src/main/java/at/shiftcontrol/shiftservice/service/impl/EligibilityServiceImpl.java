package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.Collection;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.Authorities;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotJoinErrorDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
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
    public void validateHasAccessToPositionSlot(PositionSlot positionSlot, String volunteerId) throws IllegalArgumentException {
        Collection<Volunteer> shiftPlanVolunteers = positionSlot.getShift().getShiftPlan().getPlanVolunteers();
        if (shiftPlanVolunteers.stream().noneMatch(v -> v.getId().equals(volunteerId))) {
            throw new IllegalArgumentException("Volunteer not assigned to shift plan");
        }
    }

    @Override
    public PositionSignupState getSignupStateForPositionSlot(PositionSlot positionSlot, Volunteer volunteer) {
        if (isSignedUp(positionSlot, volunteer)) {
            return PositionSignupState.SIGNED_UP;
        }

        boolean eligibleForRole = volunteer.getRoles() != null && volunteer.getRoles().contains(positionSlot.getRole());
        if (!eligibleForRole) {
            return PositionSignupState.NOT_ELIGIBLE;
        }

        boolean hasCapacity = positionSlot.getAssignments() == null || positionSlot.getAssignments().size() < positionSlot.getDesiredVolunteerCount();
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

    @Override
    public void validateSignUpStateForJoin(PositionSlot positionSlot, Volunteer volunteer) throws ConflictException {
        validateSignUpState(this.getSignupStateForPositionSlot(positionSlot, volunteer));
    }

    @Override
    public void validateSignUpStateForAuction(PositionSlot positionSlot, Volunteer volunteer) throws ConflictException {
        PositionSignupState signupState = this.getSignupStateForPositionSlot(positionSlot, volunteer);
        if (PositionSignupState.SIGNUP_VIA_AUCTION.equals(signupState)) {
            return;
        }
        validateSignUpState(signupState);
    }

    @Override
    public void validateSignUpStateForTrade(PositionSlot positionSlot, Volunteer volunteer) throws ConflictException {
        PositionSignupState signupState = this.getSignupStateForPositionSlot(positionSlot, volunteer);
        if (PositionSignupState.SIGNUP_VIA_TRADE.equals(signupState)) {
            return;
        }
        validateSignUpState(signupState);
    }

    private void validateSignUpState(PositionSignupState signupState) throws ConflictException {
        switch (signupState) {
            case SIGNED_UP:
                throw new ConflictException(PositionSlotJoinErrorDto.builder().state(signupState).build());
            case FULL:
                // Position is full
                throw new ConflictException(PositionSlotJoinErrorDto.builder().state(signupState).build());
            case NOT_ELIGIBLE:
                if (!userProvider.currentUserHasAuthority(Authorities.CAN_JOIN_UNELIGIBLE_POSITIONS)) {
                    // User is not allowed to join
                    throw new ConflictException(PositionSlotJoinErrorDto.builder().state(signupState).build());
                }
                // All good, proceed with signup
                break;
            // TODO: How to handle SIGNUP_VIA_TRADE and SIGNUP_VIA_AUCTION?
            case SIGNUP_POSSIBLE:
                // All good, proceed with signup
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + signupState);
        }
    }

    @Override
    public Collection<Assignment> getConflictingAssignments(String volunteerId, Instant startTime, Instant endTime) {
        return assignmentDao.getConflictingAssignments(volunteerId, startTime, endTime);
    }

    @Override
    public void validateHasConflictingAssignments(String volunteerId, Instant startTime, Instant endTime) throws ConflictException {
        var a = assignmentDao.getConflictingAssignments(volunteerId, startTime, endTime);
        if (a.isEmpty()) {
            return;
        }
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
        if (a.isEmpty()) {
            return;
        }
        throw new ConflictException("User has conflicting assignments");
    }

    private boolean isSignedUp(PositionSlot positionSlot, Volunteer volunteer) {
        if (positionSlot.getAssignments() == null) {
            return false;
        }

        for (var assignment : positionSlot.getAssignments()) {
            if (assignment.getAssignedVolunteer() != null && assignment.getAssignedVolunteer().getId().equals(volunteer.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAuction(PositionSlot slot) {
        return slot.getAssignments() != null && slot.getAssignments().stream()
            .anyMatch(a -> a.getStatus() == AssignmentStatus.AUCTION);
    }

    // trade requests where the requested assignment is assigned to the user
    private boolean hasOpenTradeForUser(PositionSlot slot, String userId) {
        return slot.getAssignments() != null && slot.getAssignments().stream().anyMatch(assignment ->
            assignment.getOutgoingSwitchRequests().stream().anyMatch(req ->
                req.getStatus() == TradeStatus.OPEN
                    && req.getRequestedAssignment() != null
                    && req.getRequestedAssignment().getAssignedVolunteer() != null
                    && req.getRequestedAssignment().getAssignedVolunteer().getId().equals(userId)
            )
        );
    }
}
