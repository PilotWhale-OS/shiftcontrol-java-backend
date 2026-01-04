package at.shiftcontrol.shiftservice.event.events;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.RoutingKeys;
import at.shiftcontrol.shiftservice.event.events.parts.ActivityPart;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class ActivityDeletedEvent extends BaseEvent {
    @NotNull
    private final ActivityPart activity;

    @Override
    public String getRoutingKey() {
        return RoutingKeys.formatStrict(RoutingKeys.ACTIVITY_DELETED, Map.of("activityId", String.valueOf(activity.getId())));
    }

    public static ActivityDeletedEvent of(Activity activity) {
        return new ActivityDeletedEvent(ActivityPart.of(activity));
    }
}
