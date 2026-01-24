package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.event.events.ShiftEvent;
import at.shiftcontrol.lib.event.events.parts.ShiftPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ShiftEventTest {

    @Test
    void ofInternal() {
        Shift shift = mock(Shift.class);
        String routingKey = "routingKey";

        ShiftPart shiftPart = mock(ShiftPart.class);
        try (var shiftPartMock = org.mockito.Mockito.mockStatic(ShiftPart.class)) {
            shiftPartMock.when(() -> ShiftPart.of(shift)).thenReturn(shiftPart);

            ShiftEvent shiftEvent = ShiftEvent.ofInternal(null, routingKey, shift);

            assertEquals(shiftPart, shiftEvent.getShift());
            assertEquals(routingKey, shiftEvent.getRoutingKey());
        }
    }
}

