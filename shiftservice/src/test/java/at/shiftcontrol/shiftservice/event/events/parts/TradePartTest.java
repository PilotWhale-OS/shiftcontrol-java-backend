package at.shiftcontrol.shiftservice.event.events.parts;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.AssignmentKey;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.event.events.parts.AssignmentPart;
import at.shiftcontrol.lib.event.events.parts.TradePart;
import at.shiftcontrol.lib.type.AssignmentStatus;
import at.shiftcontrol.lib.type.TradeStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TradePartTest {

    @Test
    void of() {
        // Arrange
        Volunteer volunteer1 = new Volunteer();
        volunteer1.setId("volunteer-1");
        Volunteer volunteer2 = new Volunteer();
        volunteer2.setId("volunteer-2");

        Shift shift = new Shift();
        ShiftPlan shiftPlan = new ShiftPlan();
        shift.setShiftPlan(shiftPlan);
        Event event = new Event();
        shiftPlan.setEvent(event);

        PositionSlot offeringSlot = new PositionSlot();
        offeringSlot.setId(1L);
        offeringSlot.setName("Offering Slot");
        offeringSlot.setDescription("Offering Description");
        offeringSlot.setShift(shift);

        AssignmentKey offeringId = new AssignmentKey(1L, "volunteer-1");
        Assignment offeringAssignment = new Assignment();
        offeringAssignment.setStatus(AssignmentStatus.ACCEPTED);
        offeringAssignment.setPositionSlot(offeringSlot);
        offeringAssignment.setAssignedVolunteer(volunteer1);

        PositionSlot requestedSlot = new PositionSlot();
        requestedSlot.setId(2L);
        requestedSlot.setName("Requested Slot");
        requestedSlot.setDescription("Requested Description");
        requestedSlot.setShift(shift);

        AssignmentKey requestedId = new AssignmentKey(2L, "volunteer-2");
        Assignment requestedAssignment = new Assignment();
        requestedAssignment.setStatus(AssignmentStatus.ACCEPTED);
        requestedAssignment.setPositionSlot(requestedSlot);
        requestedAssignment.setAssignedVolunteer(volunteer2);

        AssignmentSwitchRequest tradeRequest = new AssignmentSwitchRequest();
        tradeRequest.setOfferingAssignment(offeringAssignment);
        tradeRequest.setRequestedAssignment(requestedAssignment);
        tradeRequest.setStatus(TradeStatus.OPEN);
        tradeRequest.setCreatedAt(Instant.parse("2023-01-01T10:00:00Z"));

        // Act
        TradePart tradePart = TradePart.of(tradeRequest);

        // Assert
        assertNotNull(tradePart);
        assertEquals(TradeStatus.OPEN, tradePart.getStatus());
        assertEquals(tradeRequest.getCreatedAt(), tradePart.getCreatedAt());

        assertNotNull(tradePart.getOfferingAssignment());
        AssignmentPart offeringPart = tradePart.getOfferingAssignment();
        assertEquals(volunteer1.getId(), offeringPart.getVolunteerId());
        assertEquals(AssignmentStatus.ACCEPTED, offeringPart.getStatus());
        assertEquals(1L, offeringPart.getPositionSlot().getPositionSlotId());

        assertNotNull(tradePart.getRequestedAssignment());
        AssignmentPart requestedPart = tradePart.getRequestedAssignment();
        assertEquals(volunteer2.getId(), requestedPart.getVolunteerId());
        assertEquals(AssignmentStatus.ACCEPTED, requestedPart.getStatus());
        assertEquals(2L, requestedPart.getPositionSlot().getPositionSlotId());
    }

    @Test
    void of_throwsNullPointerException_forNullTradeRequest() {
        assertThrows(NullPointerException.class, () -> {
            TradePart.of(null);
        });
    }
}

