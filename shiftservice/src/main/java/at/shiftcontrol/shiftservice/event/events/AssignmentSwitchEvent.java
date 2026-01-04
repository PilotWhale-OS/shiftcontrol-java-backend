package at.shiftcontrol.shiftservice.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.AssignmentPart;

/**
 * This event is fired immediately after an assignment switch has been performed.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class AssignmentSwitchEvent extends BaseEvent {
    private final AssignmentPart requestedAssignment;
    private final AssignmentPart offeringAssignment;

    public static AssignmentSwitchEvent of(Assignment requestedAssignment, Assignment offeringAssignment) {
        return new AssignmentSwitchEvent(
            AssignmentPart.of(requestedAssignment),
            AssignmentPart.of(offeringAssignment)
        );
    }

    @Override
    public String getRoutingKey() {
        return "assignment.switch.completed." + offeringAssignment.getVolunteerId();
    }
}
