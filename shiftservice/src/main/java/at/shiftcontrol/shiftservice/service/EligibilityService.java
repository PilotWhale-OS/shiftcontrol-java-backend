package at.shiftcontrol.shiftservice.service;

import java.time.Instant;
import java.util.Collection;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.type.PositionSignupState;

public interface EligibilityService {
    /**
     * Determines the signup state of a volunteer for a given position slot.
     *
     * <p>
     * <b>This does not check if the given user has privileges for sign-up</b>
     *
     * @param positionSlot The position slot to check
     * @param volunteer    The volunteer to check
     * @return The signup state of the volunteer for the position slot
     */
    PositionSignupState getSignupStateForPositionSlot(PositionSlot positionSlot, Volunteer volunteer);

    /**
     * Determines the signup state of a volunteer for a given position slot.
     *
     * <p>
     * <b>This does not check if the given user has privileges for sign-up</b>
     *
     * @param positionSlotId The position slot to check
     * @param userId         The volunteer to check
     * @return The signup state of the volunteer for the position slot
     * @throws NotFoundException if the position slot or volunteer could not be found
     */
    PositionSignupState getSignupStateForPositionSlot(Long positionSlotId, String userId);

    /**
     * Determines the signup state of a volunteer for a given position slot.
     *
     * <p>
     * <b>This does not check if the given user has privileges for sign-up</b>
     *
     * @param positionSlot The position slot to check
     * @param userId       The volunteer to check
     * @return The signup state of the volunteer for the position slot
     */
    PositionSignupState getSignupStateForPositionSlot(PositionSlot positionSlot, String userId);

    /**
     * Checks if the volunteer can join the position slot based on PositionSignUpState.
     *
     * @param positionSlot The position slot to check
     * @param volunteer    The volunteer to check
     * @throws ConflictException if it is not possible for the volunteer to sign up for this position slot
     */
    void validateSignUpStateForJoin(PositionSlot positionSlot, Volunteer volunteer);

    /**
     * Checks if the volunteer can claim an auction for the position slot based on PositionSignUpState.
     *
     * @param positionSlot The requested position slot to check
     * @param volunteer    The volunteer to check
     * @throws ConflictException if it is not possible for the volunteer to claim an auction for this position slot
     */
    void validateSignUpStateForAuction(PositionSlot positionSlot, Volunteer volunteer);

    /**
     * Checks if the volunteer is eligible and not signed up to the given position slot.
     *
     * @param positionSlot to check for
     * @param volunteer    to assign
     * @return true if the user is not signed up and eligible
     */
    boolean isEligibleAndNotSignedUp(PositionSlot positionSlot, Volunteer volunteer);

    /**
     * Checks if the volunteer is eligible and not signed up to the given position slot.
     *
     * @param positionSlot to check for
     * @param volunteer    to assign
     * @throws ConflictException if the user is not assignable to the slot
     */
    void validateIsEligibleAndNotSignedUp(PositionSlot positionSlot, Volunteer volunteer);

    /**
     * returns all conflicting assignments within a given time for a specific user.
     *
     * @param volunteerId The volunteer to check
     * @param startTime   start of the timespan to check
     * @param endTime     end of the timespan to check
     * @return the overlapping assignments
     */
    Collection<Assignment> getConflictingAssignments(String volunteerId, Instant startTime, Instant endTime);

    /**
     * returns all conflicting assignments within a given time for a specific user.
     *
     * @param volunteerId The volunteer to check
     * @param positionSlot slot of which the time is to check
     * @return the overlapping assignments
     */
    Collection<Assignment> getConflictingAssignments(String volunteerId, PositionSlot positionSlot);

    /**
     * checks if the user has any conflicting assignments within a given time.
     *
     * @param volunteerId The volunteer to check
     * @param startTime   start of the timespan to check
     * @param endTime     end of the timespan to check
     * @throws ConflictException if overlapping assignments exist
     */
    void validateHasConflictingAssignments(String volunteerId, Instant startTime, Instant endTime);

    /**
     * checks if the user has any conflicting assignments with a given positionSlot.
     *
     * @param volunteerId  The volunteer to check
     * @param positionSlot positionSlot to check
     * @throws ConflictException if overlapping assignments exist
     */
    void validateHasConflictingAssignments(String volunteerId, PositionSlot positionSlot);

    /**
     * returns all conflicting assignments within a given time for a specific user, ignoring the provided position slot.
     *
     * @param volunteerId  The volunteer to check
     * @param startTime    start of the timespan to check
     * @param endTime      end of the timespan to check
     * @param positionSlot position slot that is ignored for the check
     * @return the overlapping assignments
     */
    Collection<Assignment> getConflictingAssignmentsExcludingSlot(String volunteerId, Instant startTime, Instant endTime, long positionSlot);

    /**
     * returns all conflicting assignments with a given position slot for a specific user, ignoring  another position slot.
     *
     * @param volunteerId  The volunteer to check
     * @param positionSlot positionSlot to check
     * @param slotToExclude position slot that is ignored for the check
     * @return the overlapping assignments
     */
    Collection<Assignment> getConflictingAssignmentsExcludingSlot(String volunteerId, PositionSlot positionSlot, long slotToExclude);

    /**
     * checks if the user has any conflicting assignments within a given time, ignoring the given position slot.
     *
     * @param volunteerId  The volunteer to check
     * @param startTime    start of the timespan to check
     * @param endTime      end of the timespan to check
     * @param positionSlot position slot that is ignored for the check
     * @throws ConflictException if overlapping assignments exist
     */
    void validateHasConflictingAssignmentsExcludingSlot(String volunteerId, Instant startTime, Instant endTime, long positionSlot);

    /**
     * checks if the user has any conflicting assignments with a given position slot, ignoring another position slot.
     *
     * @param volunteerId  The volunteer to check
     * @param positionSlot positionSlot to check
     * @param slotToExclude position slot that is ignored for the check
     * @throws ConflictException if overlapping assignments exist
     */
    void validateHasConflictingAssignmentsExcludingSlot(String volunteerId, PositionSlot positionSlot, long slotToExclude);

    /**
     * checks if a position slot is full.
     * ignores signup-requests
     *
     * @param positionSlot to check
     * @return true if slot has capacity left
     */
    boolean hasCapacity(PositionSlot positionSlot);
}
