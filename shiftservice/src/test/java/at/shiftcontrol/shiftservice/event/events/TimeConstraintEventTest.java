package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.TimeConstraint;
import at.shiftcontrol.lib.event.events.TimeConstraintEvent;
import at.shiftcontrol.lib.event.events.parts.TimeConstraintPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class TimeConstraintEventTest {

    @Test
    void of() {
        TimeConstraint timeConstraint = mock(TimeConstraint.class);
        String routingKey = "routingKey";

        TimeConstraintPart timeConstraintPart = mock(TimeConstraintPart.class);
        try (var timeConstraintPartMock = org.mockito.Mockito.mockStatic(TimeConstraintPart.class)) {
            timeConstraintPartMock.when(() -> TimeConstraintPart.of(timeConstraint)).thenReturn(timeConstraintPart);

            TimeConstraintEvent timeConstraintEvent = TimeConstraintEvent.of(routingKey, timeConstraint);

            assertEquals(routingKey, timeConstraintEvent.getRoutingKey());
            assertEquals(timeConstraintPart, timeConstraintEvent.getTimeConstraint());
        }
    }
}

