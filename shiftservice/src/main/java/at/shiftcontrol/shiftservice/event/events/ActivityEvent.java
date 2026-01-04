package at.shiftcontrol.shiftservice.event.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.ActivityPart;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class ActivityEvent extends BaseEvent {
    @NotNull
    private final ActivityPart activity;

    @JsonIgnore
    private final String routingKey;

    @Override
    public String getRoutingKey() {
        return routingKey;
    }

    public static ActivityEvent of(Activity activity, String routingKey) {
        return new ActivityEvent(ActivityPart.of(activity), routingKey);
    }
}
