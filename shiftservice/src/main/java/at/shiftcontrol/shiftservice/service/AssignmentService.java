package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotRequestDto;

public interface AssignmentService {
    /**
     * reassigns the auction to the given volunteer.
     * updates reward points & publishes event
     *
     * @param auction to claim
     * @param newVolunteer to be assigned to the auction
     * @param requestDto contains reward points hash
     * @return reassigned and accepted auction
     */
    Assignment claimAuction(Assignment auction, Volunteer newVolunteer, PositionSlotRequestDto requestDto);

    /**
     * swapps the volunteers of the given switch request.
     * cancels all involved trades of the offering and requested assignment
     * deletes inverse trade if present
     * updates reward points & publishes event
     *
     * @param oldTrade trade to execute
     * @return executed trade, where volunteers are swapped
     */
    AssignmentSwitchRequest executeTrade(AssignmentSwitchRequest oldTrade);

    /**
     * accepts an already existing assignment.
     * assumes that eligibility, conflicts, etc. have already been checked
     * updates reward points & publishes event
     *
     * @param assignment to accept
     * @return the accepted assignment
     */
    Assignment accept(Assignment assignment);

    /**
     * assigns the volunteer to the position slot.
     * assumes that eligibility, conflicts, etc. have already been checked
     * cancels trades where this slot was offered to the volunteer
     * updates reward points & publishes event
     *
     * @param positionSlot to assign the volunteer to
     * @param volunteer to assign to the position slot
     * @return the newly created assignment
     */
    Assignment assign(PositionSlot positionSlot, Volunteer volunteer, PositionSlotRequestDto requestDto);

    /**
     * unassigns the volunteer from the position slot.
     * deletes dependent trades and the assignment itself in the process
     * updates reward points & publishes event
     *
     * @param assignment to dissolve
     */
    void unassign(Assignment assignment);

    /**
     * unassign all volunteers from auctions for a given shift plan.
     * deletes all involved trades in the process
     * updates reward points & publishes event
     *
     * @param shiftPlan to unassign all auctions
     */
    void unassignAllAuctions(ShiftPlan shiftPlan);
}
