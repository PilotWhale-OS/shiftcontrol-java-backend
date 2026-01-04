package at.shiftcontrol.shiftservice.event.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.entity.TimeConstraint;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.TimeConstraintPart;

@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class TimeConstraintEvent extends BaseEvent {
    @JsonIgnore
    private final String routingKey;

    private final TimeConstraintPart timeConstraint;

    public static TimeConstraintEvent of(TimeConstraint timeConstraint, String routingKey) {
        return new TimeConstraintEvent(routingKey, TimeConstraintPart.of(timeConstraint));
    }
}
