package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.event.events.parts.ShiftPlanPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ShiftPlanEventTest {

    @Test
    void of() {
        ShiftPlan shiftPlan = mock(ShiftPlan.class);
        String routingKey = "routingKey";

        ShiftPlanPart shiftPlanPart = mock(ShiftPlanPart.class);
        try (var shiftPlanPartMock = org.mockito.Mockito.mockStatic(ShiftPlanPart.class)) {
            shiftPlanPartMock.when(() -> ShiftPlanPart.of(shiftPlan)).thenReturn(shiftPlanPart);

            ShiftPlanEvent shiftPlanEvent = ShiftPlanEvent.of(routingKey, shiftPlan);

            assertEquals(routingKey, shiftPlanEvent.getRoutingKey());
            assertEquals(shiftPlanPart, shiftPlanEvent.getShiftPlan());
        }
    }
}

