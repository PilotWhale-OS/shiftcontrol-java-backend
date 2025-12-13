package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.exception.NotFoundException;
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
    PositionSignupState getSignupStateForPositionSlot(Long positionSlotId, Long userId) throws NotFoundException;
}
