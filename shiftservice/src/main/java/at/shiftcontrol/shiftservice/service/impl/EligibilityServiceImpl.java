package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.Authorities;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.PositionSlotJoinErrorDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.repo.AssignmentRepository;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.type.PositionSignupState;

@Service
@RequiredArgsConstructor
public class EligibilityServiceImpl implements EligibilityService {
    private final VolunteerDao volunteerDao;
    private final PositionSlotDao positionSlotDao;

    private final AssignmentRepository assignmentRepository;

    private final ApplicationUserProvider userProvider;

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
        if (positionSlot.getAssignments().size() >= positionSlot.getDesiredVolunteerCount()) {
            return PositionSignupState.FULL;
        }
        if (isSignedUp(positionSlot, volunteer)) {
            return PositionSignupState.SIGNED_UP;
        }
        if (!volunteer.getRoles().contains(positionSlot.getRole())) {
            return PositionSignupState.NOT_ELIGIBLE;
        }
        return PositionSignupState.SIGNUP_POSSIBLE;
    }

    @Override
    public void validateSignUpStateForJoin(PositionSlot positionSlot, Volunteer volunteer) throws ConflictException {
        validateSignUpState(this.getSignupStateForPositionSlot(positionSlot, volunteer));
    }

    @Override
    public void validateSignUpStateForTrade(PositionSlot positionSlot, Volunteer volunteer) throws ConflictException {
        PositionSignupState signupState = this.getSignupStateForPositionSlot(positionSlot, volunteer);
        if (PositionSignupState.FULL.equals(signupState)) return; // trades can be created even if the position slot is full
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
            case SIGNUP_POSSIBLE:
                // All good, proceed with signup
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + signupState);
        }
    }

    @Override
    public Collection<Assignment> getConflictingAssignments(long volunteerId, Instant startTime, Instant endTime) {
        return assignmentRepository.getConflictingAssignments(volunteerId, startTime, endTime);
    }

    @Override
    public void validateHasConflictingAssignments(long volunteerId, Instant startTime, Instant endTime) throws ConflictException {
        var a = assignmentRepository.getConflictingAssignments(volunteerId, startTime, endTime);
        if (a.isEmpty()) return;
        throw new ConflictException("User has conflicting assignments");
    }

    @Override
    public Collection<Assignment> getConflictingAssignmentsExcludingSlot(long volunteerId, Instant startTime, Instant endTime, long positionSlot) {
        return assignmentRepository.getConflictingAssignments(volunteerId, startTime, endTime);
    }

    @Override
    public void validateHasConflictingAssignmentsExcludingSlot(long volunteerId, Instant startTime, Instant endTime, long positionSlot) throws ConflictException {
        var a = assignmentRepository.getConflictingAssignmentsExcludingSlot(volunteerId, startTime, endTime, positionSlot);
        if (a.isEmpty()) return;
        throw new ConflictException("User has conflicting assignments");
    }

    private boolean isSignedUp(PositionSlot positionSlot, Volunteer volunteer) {
        for (var assignment : positionSlot.getAssignments()) {
            if (assignment.getAssignedVolunteer().getId() == volunteer.getId()) {
                return true;
            }
        }
        return false;
    }
}
