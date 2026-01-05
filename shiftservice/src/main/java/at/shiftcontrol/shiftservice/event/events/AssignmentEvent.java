package at.shiftcontrol.shiftservice.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.AssignmentPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class AssignmentEvent extends BaseEvent {
    private final AssignmentPart assignment;

    public AssignmentEvent(String routingKey, AssignmentPart assignment) {
        super(routingKey);
        this.assignment = assignment;
    }

    public static AssignmentEvent of(String routingKey, Assignment assignment) {
        return new AssignmentEvent(routingKey, AssignmentPart.of(assignment));
    }
}
