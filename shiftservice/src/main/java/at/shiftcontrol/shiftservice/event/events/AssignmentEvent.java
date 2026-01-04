package at.shiftcontrol.shiftservice.event.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.AssignmentPart;

@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class AssignmentEvent extends BaseEvent {
    @JsonIgnore
    private final String routingKey;

    private final AssignmentPart assignment;

    public static AssignmentEvent of(Assignment assignment, String routingKey) {
        return new AssignmentEvent(routingKey, AssignmentPart.of(assignment));
    }
}
