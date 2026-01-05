package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.event.events.parts.AssignmentPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AssignmentEventTest {

    @Test
    void of() {
        Assignment assignment = mock(Assignment.class);
        String routingKey = "routingKey";

        AssignmentPart assignmentPart = mock(AssignmentPart.class);
        try (var assignmentPartMock = org.mockito.Mockito.mockStatic(AssignmentPart.class)) {
            assignmentPartMock.when(() -> AssignmentPart.of(assignment)).thenReturn(assignmentPart);

            AssignmentEvent assignmentEvent = AssignmentEvent.of(assignment, routingKey);

            assertEquals(assignmentPart, assignmentEvent.getAssignment());
            assertEquals(routingKey, assignmentEvent.getRoutingKey());
        }
    }
}

