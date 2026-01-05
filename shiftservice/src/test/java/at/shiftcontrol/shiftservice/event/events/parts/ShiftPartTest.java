package at.shiftcontrol.shiftservice.event.events.parts;

import java.time.Instant;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.entity.Location;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShiftPartTest {

    @Test
    void of_withRelations() {
        // Arrange
        Activity activity = new Activity();
        activity.setId(1L);

        Location location = new Location();
        location.setId(1L);

        PositionSlot positionSlot = new PositionSlot();
        positionSlot.setId(1L);
        positionSlot.setName("Test Slot");
        positionSlot.setDescription("Test Description");

        Shift shift = new Shift();
        shift.setId(1L);
        shift.setName("Test Shift");
        shift.setShortDescription("Short Description");
        shift.setLongDescription("Long Description");
        shift.setStartTime(Instant.parse("2023-01-01T10:00:00Z"));
        shift.setEndTime(Instant.parse("2023-01-01T12:00:00Z"));
        shift.setRelatedActivity(activity);
        shift.setLocation(location);
        shift.setSlots(Collections.singletonList(positionSlot));

        // Act
        ShiftPart shiftPart = ShiftPart.of(shift);

        // Assert
        assertEquals(String.valueOf(shift.getId()), shiftPart.getId());
        assertEquals(shift.getName(), shiftPart.getName());
        assertEquals(shift.getShortDescription(), shiftPart.getShortDescription());
        assertEquals(shift.getLongDescription(), shiftPart.getLongDescription());
        assertEquals(shift.getStartTime(), shiftPart.getStartTime());
        assertEquals(shift.getEndTime(), shiftPart.getEndTime());
        assertEquals(activity.getId(), shiftPart.getRelatedActivityId());
        assertEquals(location.getId(), shiftPart.getLocationId());

        assertNotNull(shiftPart.getPositionSlots());
        assertEquals(1, shiftPart.getPositionSlots().size());
        PositionSlotPart slotPart = shiftPart.getPositionSlots().iterator().next();
        assertEquals(positionSlot.getId(), slotPart.getPositionSlotId());
        assertEquals(positionSlot.getName(), slotPart.getPositionSlotName());
        assertEquals(positionSlot.getDescription(), slotPart.getPositionSlotDescription());
    }

    @Test
    void of_withNullRelations() {
        // Arrange
        Shift shift = new Shift();
        shift.setId(1L);
        shift.setName("Test Shift");
        shift.setStartTime(Instant.now());
        shift.setEndTime(Instant.now());
        shift.setRelatedActivity(null);
        shift.setLocation(null);
        shift.setSlots(Collections.emptyList());

        // Act
        ShiftPart shiftPart = ShiftPart.of(shift);

        // Assert
        assertEquals(String.valueOf(shift.getId()), shiftPart.getId());
        assertEquals(null, shiftPart.getRelatedActivityId());
        assertEquals(null, shiftPart.getLocationId());
        assertTrue(shiftPart.getPositionSlots().isEmpty());
    }

    @Test
    void of_throwsNullPointerException_forNullShift() {
        assertThrows(NullPointerException.class, () -> {
            ShiftPart.of(null);
        });
    }
}

