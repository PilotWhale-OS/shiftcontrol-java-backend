package at.shiftcontrol.shiftservice.event.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.event.ApplicationEvent;
import at.shiftcontrol.shiftservice.event.EventClassifier;
import at.shiftcontrol.shiftservice.event.events.parts.AssignmentPart;
import static at.shiftcontrol.shiftservice.event.EventType.ASSIGNMENT_SWITCH;

/**
 * This event is fired immediately after an assignment switch has been performed.
 */
@Data
@Builder
@AllArgsConstructor
@EventClassifier(ASSIGNMENT_SWITCH)
public class AssignmentSwitchEvent extends ApplicationEvent {
    private AssignmentPart requestedAssignment;
    private AssignmentPart offeringAssignment;

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
