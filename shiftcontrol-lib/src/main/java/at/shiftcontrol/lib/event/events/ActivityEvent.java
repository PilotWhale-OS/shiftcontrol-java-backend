package at.shiftcontrol.lib.event.events;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Activity;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.ActivityPart;

@EqualsAndHashCode(callSuper = true)
@Data
public class ActivityEvent extends BaseEvent {
    @NotNull
    private final ActivityPart activity;

    public ActivityEvent(String routingKey, ActivityPart activity) {
        super(routingKey);
        this.activity = activity;
    }

    private static ActivityEvent of(String routingKey, Activity activity) {
        return new ActivityEvent(routingKey, ActivityPart.of(activity));
    }

    public static ActivityEvent forCreated(Activity activity) {
        return of(RoutingKeys.ACTIVITY_CREATED, activity);
    }

    public static ActivityEvent forUpdated(Activity activity) {
        return of(RoutingKeys.format(RoutingKeys.ACTIVITY_UPDATED,
            Map.of("activityId", String.valueOf(activity.getId()))), activity);
    }
}
