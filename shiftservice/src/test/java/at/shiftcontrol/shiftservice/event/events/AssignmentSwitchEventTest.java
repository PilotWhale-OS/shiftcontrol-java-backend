package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.event.events.AssignmentSwitchEvent;
import at.shiftcontrol.lib.event.events.parts.AssignmentPart;
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
        when(requestedAssignmentPart.getVolunteerId()).thenReturn("1");
        when(offeringAssignmentPart.getVolunteerId()).thenReturn("2");

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
        AssignmentPart requestedAssignmentPart = mock(AssignmentPart.class);
        AssignmentPart offeringAssignmentPart = mock(AssignmentPart.class);
        when(requestedAssignmentPart.getVolunteerId()).thenReturn("1");
        when(offeringAssignmentPart.getVolunteerId()).thenReturn("2");
        AssignmentSwitchEvent assignmentSwitchEvent = new AssignmentSwitchEvent(requestedAssignmentPart, offeringAssignmentPart);

        assertEquals("trade.request.completed.1.2", assignmentSwitchEvent.getRoutingKey());
    }
}

