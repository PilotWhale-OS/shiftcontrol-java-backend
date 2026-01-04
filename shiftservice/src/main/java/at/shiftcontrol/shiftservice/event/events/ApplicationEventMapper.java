package at.shiftcontrol.shiftservice.event.events;

import org.shiftcontrol.lib.asyncapi.events.parts.AssignmentPart;
import org.shiftcontrol.lib.asyncapi.events.parts.PositionSlotPart;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.PositionSlot;

public class ApplicationEventMapper {
    public static AssignmentPart toAssignmentPart(Assignment assignment) {
        return new AssignmentPart()
            .withVolunteerId(assignment.getId().getVolunteerId())
            .withStatus(AssignmentPart.Status.fromValue(assignment.getStatus().toString()))
            .withPositionSlot(toPositionSlotPart(assignment.getPositionSlot()));
    }

    public static PositionSlotPart toPositionSlotPart(PositionSlot positionSlot) {
        return new PositionSlotPart()
            .withPositionSlotId(positionSlot.getId())
            .withPositionSlotName(positionSlot.getName())
            .withPositionSlotDescription(positionSlot.getDescription());
    }
}
