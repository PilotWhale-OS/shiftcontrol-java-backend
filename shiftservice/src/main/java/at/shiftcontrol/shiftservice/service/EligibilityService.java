package at.shiftcontrol.shiftservice.service;

import java.time.Instant;
import java.util.Collection;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.type.PositionSignupState;

public interface EligibilityService {
    /**
     *  Determines the signup state of a volunteer for a given position slot.
     *
     *  <p>
     *  <b>This does not check if the given user has privileges for sign-up</b>
     *
     * @param positionSlot The position slot to check
     * @param volunteer The volunteer to check
     * @return The signup state of the volunteer for the position slot
     */
    PositionSignupState getSignupStateForPositionSlot(PositionSlot positionSlot, Volunteer volunteer);

    /**
     *  Determines the signup state of a volunteer for a given position slot.
     *
     *  <p>
     *  <b>This does not check if the given user has privileges for sign-up</b>
     *
     * @param positionSlotId The position slot to check
     * @param userId The volunteer to check
     * @return The signup state of the volunteer for the position slot
     * @throws NotFoundException if the position slot or volunteer could not be found
     */
    PositionSignupState getSignupStateForPositionSlot(Long positionSlotId, String userId) throws NotFoundException;

    /**
     * Checks if the volunteer can join the position slot based on PositionSignUpState
     *
     * @param positionSlot The position slot to check
     * @param volunteer The volunteer to check
     * @throws ConflictException if it is not possible for the volunteer to sign up for this position slot
     */
    void validateSignUpStateForJoin(PositionSlot positionSlot, Volunteer volunteer) throws ConflictException;

    /**
     * Checks if the volunteer can request a trade for the position slot based on PositionSignUpState
     *
     * @param positionSlot The requested position slot to check
     * @param volunteer The volunteer to check
     * @throws ConflictException if it is not possible for the volunteer to create or accept a trade for this position slot
     */
    void validateSignUpStateForTrade(PositionSlot positionSlot, Volunteer volunteer) throws ConflictException;

    /**
     * returns all conflicting assignments within a given time for a specific user
     *
     * @param volunteerId The volunteer to check
     * @param startTime start of the timespan to check
     * @param endTime end of the timespan to check
     * @return the overlapping assignments
     */
    Collection<Assignment> getConflictingAssignments(String volunteerId, Instant startTime, Instant endTime);

    /**
     * checks if the user has any conflicting assignments within a given time
     *
     * @param volunteerId The volunteer to check
     * @param startTime start of the timespan to check
     * @param endTime end of the timespan to check
     * @throws ConflictException if overlapping assignments exist
     */
    void validateHasConflictingAssignments(String volunteerId, Instant startTime, Instant endTime) throws ConflictException;

    /**
     * returns all conflicting assignments within a given time for a specific user, ignoring the provided position slot
     *
     * @param volunteerId The volunteer to check
     * @param startTime start of the timespan to check
     * @param endTime end of the timespan to check
     * @param positionSlot position slot that is ignored for the check
     * @return the overlapping assignments
     */
    Collection<Assignment> getConflictingAssignmentsExcludingSlot(String volunteerId, Instant startTime, Instant endTime, long positionSlot);

    /**
     * checks if the user has any conflicting assignments within a given time, ignoring the provided position slot
     *
     * @param volunteerId The volunteer to check
     * @param startTime start of the timespan to check
     * @param endTime end of the timespan to check
     * @param positionSlot position slot that is ignored for the check
     * @throws ConflictException if overlapping assignments exist
     */
    void validateHasConflictingAssignmentsExcludingSlot(String volunteerId, Instant startTime, Instant endTime, long positionSlot) throws ConflictException;

    /**
     *  Determines the signup state of a volunteer for a given position slot.
     *
     *  <p>
     *  <b>This does not check if the given user has privileges for sign-up</b>
     *
     * @param positionSlot The position slot to check
     * @param userId The volunteer to check
     * @return The signup state of the volunteer for the position slot
     */
    PositionSignupState getSignupStateForPositionSlot(PositionSlot positionSlot, String userId) throws NotFoundException;
}
