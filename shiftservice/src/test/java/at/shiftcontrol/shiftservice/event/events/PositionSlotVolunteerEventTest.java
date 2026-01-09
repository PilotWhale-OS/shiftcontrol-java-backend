package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.lib.event.events.parts.PositionSlotPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PositionSlotVolunteerEventTest {

    @Test
    void of() {
        String routingKey = "routingKey";
        PositionSlot positionSlot = mock(PositionSlot.class);
        String volunteerId = "volunteerId";

        PositionSlotPart positionSlotPart = mock(PositionSlotPart.class);
        try (var positionSlotPartMock = org.mockito.Mockito.mockStatic(PositionSlotPart.class)) {
            positionSlotPartMock.when(() -> PositionSlotPart.of(positionSlot)).thenReturn(positionSlotPart);

            PositionSlotVolunteerEvent positionSlotVolunteerEvent = PositionSlotVolunteerEvent.of(routingKey, positionSlot, volunteerId);

            assertEquals(routingKey, positionSlotVolunteerEvent.getRoutingKey());
            assertEquals(positionSlotPart, positionSlotVolunteerEvent.getPositionSlot());
            assertEquals(volunteerId, positionSlotVolunteerEvent.getVolunteerId());
        }
    }
}

