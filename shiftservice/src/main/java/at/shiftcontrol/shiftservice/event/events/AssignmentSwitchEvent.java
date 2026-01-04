package at.shiftcontrol.shiftservice.event.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.AssignmentPart;

/**
 * This event is fired immediately after an assignment switch has been performed.
 */
@Data
@Builder
@AllArgsConstructor
public class AssignmentSwitchEvent extends BaseEvent {
    private final AssignmentPart requestedAssignment;
    private final AssignmentPart offeringAssignment;

    public static AssignmentSwitchEvent of(Assignment requestedAssignment, Assignment offeringAssignment) {
        return AssignmentSwitchEvent.builder()
                .requestedAssignment(AssignmentPart.of(requestedAssignment))
                .offeringAssignment(AssignmentPart.of(offeringAssignment))
                .build();
    }

    @Override
    public String getRoutingKey() {
        return "assignment.switch.completed." + offeringAssignment.getVolunteerId();
    }
}
