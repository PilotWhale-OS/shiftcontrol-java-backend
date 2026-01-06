package at.shiftcontrol.shiftservice.event.events.parts;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.PositionSlot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionSlotPartTest {

    @Test
    void of() {
        // Arrange
        PositionSlot positionSlot = new PositionSlot();
        positionSlot.setId(1L);
        positionSlot.setName("Test Position Slot");
        positionSlot.setDescription("Test Description");

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

