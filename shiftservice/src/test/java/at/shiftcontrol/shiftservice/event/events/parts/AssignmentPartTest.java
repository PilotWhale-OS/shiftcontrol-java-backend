package at.shiftcontrol.shiftservice.event.events.parts;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.event.events.parts.AssignmentPart;
import at.shiftcontrol.lib.event.events.parts.PositionSlotPart;
import at.shiftcontrol.lib.type.AssignmentStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssignmentPartTest {

    @Test
    void of() {
        // Arrange
        PositionSlot positionSlot = new PositionSlot();
        positionSlot.setId(1L);
        positionSlot.setName("Test Position Slot");
        positionSlot.setDescription("Test Description");
        Volunteer volunteer = new Volunteer();
        volunteer.setId("volunteer-123");

        Assignment assignment = new Assignment();
        assignment.setStatus(AssignmentStatus.REQUEST_FOR_ASSIGNMENT);
        assignment.setPositionSlot(positionSlot);
        assignment.setAssignedVolunteer(volunteer);

        // Act
        AssignmentPart assignmentPart = AssignmentPart.of(assignment);

        // Assert
        assertNotNull(assignmentPart);
        assertEquals(volunteer.getId(), assignmentPart.getVolunteerId());
        assertEquals(AssignmentStatus.REQUEST_FOR_ASSIGNMENT, assignmentPart.getStatus());

        assertNotNull(assignmentPart.getPositionSlot());
        PositionSlotPart positionSlotPart = assignmentPart.getPositionSlot();
        assertEquals(positionSlot.getId(), positionSlotPart.getPositionSlotId());
        assertEquals(positionSlot.getName(), positionSlotPart.getPositionSlotName());
        assertEquals(positionSlot.getDescription(), positionSlotPart.getPositionSlotDescription());
    }

    @Test
    void of_throwsNullPointerException_forNullAssignment() {
        assertThrows(NullPointerException.class, () -> {
            AssignmentPart.of(null);
        });
    }
}

