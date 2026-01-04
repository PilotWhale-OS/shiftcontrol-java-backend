package at.shiftcontrol.shiftservice.event.events;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.event.ApplicationEvent;
import at.shiftcontrol.shiftservice.event.RoutingKeys;
import at.shiftcontrol.shiftservice.event.events.parts.ActivityPart;

@RequiredArgsConstructor
public class ActivityCreatedEvent extends ApplicationEvent {
    @NotNull
    private final ActivityPart activity;

    @Override
    public String getRoutingKey() {
        return RoutingKeys.ACTIVITY_CREATED;
    }
}
