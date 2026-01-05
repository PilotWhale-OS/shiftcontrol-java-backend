package at.shiftcontrol.shiftservice.event.events;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.RoutingKeys;
import at.shiftcontrol.shiftservice.event.events.parts.AssignmentPart;

/**
 * This event is fired immediately after an assignment switch has been performed.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssignmentSwitchEvent extends BaseEvent {
    private final AssignmentPart requestedAssignment;
    private final AssignmentPart offeringAssignment;

    public AssignmentSwitchEvent(AssignmentPart requestedAssignment, AssignmentPart offeringAssignment) {
        super(RoutingKeys.format(RoutingKeys.ASSIGNMENT_SWITCH_COMPLETED,
            Map.of("requestedVolunteerId", requestedAssignment.getVolunteerId(),
                   "offeringVolunteerId", offeringAssignment.getVolunteerId())));
        this.requestedAssignment = requestedAssignment;
        this.offeringAssignment = offeringAssignment;
    }

    public static AssignmentSwitchEvent of(Assignment requestedAssignment, Assignment offeringAssignment) {
        return new AssignmentSwitchEvent(
            AssignmentPart.of(requestedAssignment),
            AssignmentPart.of(offeringAssignment)
        );
    }
}
