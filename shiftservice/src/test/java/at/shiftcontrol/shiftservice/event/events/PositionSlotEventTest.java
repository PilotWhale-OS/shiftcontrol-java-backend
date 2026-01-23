package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.event.events.PositionSlotEvent;
import at.shiftcontrol.lib.event.events.parts.PositionSlotPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PositionSlotEventTest {

    @Test
    void ofInternal() {
        PositionSlot positionSlot = mock(PositionSlot.class);
        String routingKey = "routingKey";

        PositionSlotPart positionSlotPart = mock(PositionSlotPart.class);
        try (var positionSlotPartMock = org.mockito.Mockito.mockStatic(PositionSlotPart.class)) {
            positionSlotPartMock.when(() -> PositionSlotPart.of(positionSlot)).thenReturn(positionSlotPart);

            PositionSlotEvent positionSlotEvent = PositionSlotEvent.ofInternal(routingKey, positionSlot);

            assertEquals(routingKey, positionSlotEvent.getRoutingKey());
            assertEquals(positionSlotPart, positionSlotEvent.getPositionSlot());
        }
    }
}

