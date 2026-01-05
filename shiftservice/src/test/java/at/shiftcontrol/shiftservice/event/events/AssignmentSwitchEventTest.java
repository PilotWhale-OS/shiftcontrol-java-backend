package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.event.events.parts.AssignmentPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssignmentSwitchEventTest {

    @Test
    void of() {
        Assignment requestedAssignment = mock(Assignment.class);
        Assignment offeringAssignment = mock(Assignment.class);

        AssignmentPart requestedAssignmentPart = mock(AssignmentPart.class);
        AssignmentPart offeringAssignmentPart = mock(AssignmentPart.class);

        try (var assignmentPartMock = org.mockito.Mockito.mockStatic(AssignmentPart.class)) {
            assignmentPartMock.when(() -> AssignmentPart.of(requestedAssignment)).thenReturn(requestedAssignmentPart);
            assignmentPartMock.when(() -> AssignmentPart.of(offeringAssignment)).thenReturn(offeringAssignmentPart);

            AssignmentSwitchEvent assignmentSwitchEvent = AssignmentSwitchEvent.of(requestedAssignment, offeringAssignment);

            assertEquals(requestedAssignmentPart, assignmentSwitchEvent.getRequestedAssignment());
            assertEquals(offeringAssignmentPart, assignmentSwitchEvent.getOfferingAssignment());
        }
    }

    @Test
    void getRoutingKey() {
        AssignmentPart offeringAssignmentPart = mock(AssignmentPart.class);
        when(offeringAssignmentPart.getVolunteerId()).thenReturn("1");
        AssignmentSwitchEvent assignmentSwitchEvent = new AssignmentSwitchEvent(mock(AssignmentPart.class), offeringAssignmentPart);

        assertEquals("assignment.switch.completed.1", assignmentSwitchEvent.getRoutingKey());
    }
}

