package at.shiftcontrol.lib.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Location;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.events.parts.LocationPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocationEvent extends BaseEvent {
    private final LocationPart location;

    public LocationEvent(String routingKey, LocationPart location) {
        super(routingKey);
        this.location = location;
    }

    public static LocationEvent of(String routingKey, Location location) {
        return new LocationEvent(routingKey, LocationPart.of(location));
    }
}
