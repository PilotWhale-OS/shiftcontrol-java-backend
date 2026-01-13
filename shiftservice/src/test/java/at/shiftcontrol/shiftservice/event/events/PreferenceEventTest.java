package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.event.events.PreferenceEvent;
import at.shiftcontrol.lib.event.events.parts.PositionSlotPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PreferenceEventTest {

    @Test
    void of() {
        String routingKey = "routingKey";
        String volunteerId = "volunteerId";
        int preferenceLevel = 1;
        PositionSlot positionSlot = mock(PositionSlot.class);

        PositionSlotPart positionSlotPart = mock(PositionSlotPart.class);
        try (var positionSlotPartMock = org.mockito.Mockito.mockStatic(PositionSlotPart.class)) {
            positionSlotPartMock.when(() -> PositionSlotPart.of(positionSlot)).thenReturn(positionSlotPart);

            PreferenceEvent preferenceEvent = PreferenceEvent.of(routingKey, volunteerId, preferenceLevel, positionSlot);

            assertEquals(routingKey, preferenceEvent.getRoutingKey());
            assertEquals(volunteerId, preferenceEvent.getVolunteerId());
            assertEquals(preferenceLevel, preferenceEvent.getPreferenceLevel());
            assertEquals(positionSlotPart, preferenceEvent.getPositionSlot());
        }
    }
}

