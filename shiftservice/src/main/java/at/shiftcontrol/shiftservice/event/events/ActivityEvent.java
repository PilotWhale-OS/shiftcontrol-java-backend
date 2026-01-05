package at.shiftcontrol.shiftservice.event.events;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.ActivityPart;

@EqualsAndHashCode(callSuper = true)
@Data
public class ActivityEvent extends BaseEvent {
    @NotNull
    private final ActivityPart activity;

    public ActivityEvent(String routingKey, ActivityPart activity) {
        super(routingKey);
        this.activity = activity;
    }

    public static ActivityEvent of(String routingKey, Activity activity) {
        return new ActivityEvent(routingKey, ActivityPart.of(activity));
    }
}
