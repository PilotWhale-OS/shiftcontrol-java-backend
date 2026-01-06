package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.event.events.parts.ShiftPlanPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ShiftPlanVolunteerEventTest {

    @Test
    void of() {
        String routingKey = "routingKey";
        ShiftPlan shiftPlan = mock(ShiftPlan.class);
        String volunteerId = "volunteerId";

        ShiftPlanPart shiftPlanPart = mock(ShiftPlanPart.class);
        try (var shiftPlanPartMock = org.mockito.Mockito.mockStatic(ShiftPlanPart.class)) {
            shiftPlanPartMock.when(() -> ShiftPlanPart.of(shiftPlan)).thenReturn(shiftPlanPart);

            ShiftPlanVolunteerEvent shiftPlanVolunteerEvent = ShiftPlanVolunteerEvent.of(routingKey, shiftPlan, volunteerId);

            assertEquals(routingKey, shiftPlanVolunteerEvent.getRoutingKey());
            assertEquals(shiftPlanPart, shiftPlanVolunteerEvent.getShiftPlan());
            assertEquals(volunteerId, shiftPlanVolunteerEvent.getVolunteerId());
        }
    }
}

