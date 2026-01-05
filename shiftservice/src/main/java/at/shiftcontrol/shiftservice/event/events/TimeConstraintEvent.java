package at.shiftcontrol.shiftservice.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.shiftservice.entity.TimeConstraint;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.TimeConstraintPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class TimeConstraintEvent extends BaseEvent {
    private final TimeConstraintPart timeConstraint;

    public TimeConstraintEvent(String routingKey, TimeConstraintPart timeConstraint) {
        super(routingKey);
        this.timeConstraint = timeConstraint;
    }

    public static TimeConstraintEvent of(String routingKey, TimeConstraint timeConstraint) {
        return new TimeConstraintEvent(routingKey, TimeConstraintPart.of(timeConstraint));
    }
}
