package at.shiftcontrol.trustservice.util;

import java.util.Map;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.AssignmentEvent;
import at.shiftcontrol.lib.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.lib.event.events.TradeEvent;

public class TestEntityFactory {

    public static PositionSlotVolunteerEvent getPositionSlotVolunteerEvent(String routingKey, String userId, long slotId) {
        PositionSlot positionSlot = getPositionSlot(slotId);
        return PositionSlotVolunteerEvent.ofInternal(null, RoutingKeys.format(routingKey,
                Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                    "volunteerId", userId)),
            positionSlot, userId);
    }

    public  static AssignmentEvent getAssignmentEvent(String routingKey, String userId, long slotId) {
        Assignment assignment = getAssignment(userId, slotId);
        return AssignmentEvent.of(null, routingKey, assignment);
    }

    public  static TradeEvent getTradeEvent(String routingKey, String offeringUserId, long offeringSlotId,
                                     String requestingUserId, long requestingSlotId) {
        return TradeEvent.ofInternal(null, routingKey, getAssignmentSwitchRequest(offeringUserId, offeringSlotId, requestingUserId, requestingSlotId));
    }

    private static AssignmentSwitchRequest getAssignmentSwitchRequest(String offeringUserId, long offeringSlotId,
                                                               String requestingUserId, long requestingSlotId) {
        Assignment offering = getAssignment(offeringUserId, offeringSlotId);
        Assignment requesting = getAssignment(requestingUserId, requestingSlotId);
        return AssignmentSwitchRequest.builder()
            .offeringAssignment(offering)
            .requestedAssignment(requesting)
            .build();
    }

    private static Assignment getAssignment(String volunteerId, long slotId) {
        return Assignment.builder()
            .assignedVolunteer(getVolunteer(volunteerId))
            .positionSlot(getPositionSlot(slotId))
            .build();
    }

    private static Volunteer getVolunteer(String id) {
        return Volunteer.builder()
            .id(id)
            .build();
    }

    private static PositionSlot getPositionSlot(long id) {
        return PositionSlot.builder()
            .id(id)
            .shift(getSetupShift())
            .build();
    }

    private static Shift getSetupShift() {
        Shift shift = new Shift();
        ShiftPlan shiftPlan = new ShiftPlan();
        shift.setShiftPlan(shiftPlan);
        Event event = new Event();
        shiftPlan.setEvent(event);
        return shift;
    }
}
