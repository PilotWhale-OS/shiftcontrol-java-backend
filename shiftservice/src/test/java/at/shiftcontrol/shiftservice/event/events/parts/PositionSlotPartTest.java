package at.shiftcontrol.shiftservice.event.events.parts;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.event.events.parts.PositionSlotPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionSlotPartTest {

    @Test
    void of() {
        // Arrange
        Shift shift = new Shift();
        ShiftPlan shiftPlan = new ShiftPlan();
        shift.setShiftPlan(shiftPlan);
        Event event = new Event();
        shiftPlan.setEvent(event);
        PositionSlot positionSlot = new PositionSlot();
        positionSlot.setId(1L);
        positionSlot.setName("Test Position Slot");
        positionSlot.setDescription("Test Description");
        positionSlot.setShift(shift);

        // Act
        PositionSlotPart positionSlotPart = PositionSlotPart.of(positionSlot);

        // Assert
        assertEquals(positionSlot.getId(), positionSlotPart.getPositionSlotId());
        assertEquals(positionSlot.getName(), positionSlotPart.getPositionSlotName());
        assertEquals(positionSlot.getDescription(), positionSlotPart.getPositionSlotDescription());
    }

    @Test
    void of_throwsNullPointerException_forNullPositionSlot() {
        assertThrows(NullPointerException.class, () -> {
            PositionSlotPart.of(null);
        });
    }
}

