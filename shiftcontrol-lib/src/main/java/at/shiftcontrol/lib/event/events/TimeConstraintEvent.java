package at.shiftcontrol.lib.event.events;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.TimeConstraint;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.TimeConstraintPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class TimeConstraintEvent extends BaseEvent {
    private final TimeConstraintPart timeConstraint;

    public TimeConstraintEvent(String routingKey, TimeConstraintPart timeConstraint) {
        super(routingKey);
        this.timeConstraint = timeConstraint;
    }

    public static TimeConstraintEvent ofInternal(String routingKey, TimeConstraint timeConstraint) {
        return new TimeConstraintEvent(routingKey, TimeConstraintPart.of(timeConstraint));
    }

    public static TimeConstraintEvent timeConstraintCreated(TimeConstraint timeConstraint) {
        return ofInternal(RoutingKeys.TIMECONSTRAINT_CREATED, timeConstraint);
    }

    public static TimeConstraintEvent timeConstraintDeleted(TimeConstraint timeConstraint) {
        return ofInternal(RoutingKeys.format(RoutingKeys.TIMECONSTRAINT_DELETED, Map.of(
            "timeConstraintId", String.valueOf(timeConstraint.getId()),
            "volunteerId", timeConstraint.getVolunteer().getId())),
            timeConstraint);
    }
}
