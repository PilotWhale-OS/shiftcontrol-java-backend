package at.shiftcontrol.lib.event.events;

import at.shiftcontrol.lib.event.RoutingKeys;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Location;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.events.parts.LocationPart;

import java.util.Map;

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

    public static LocationEvent forCreated(Location location) {
        return of(RoutingKeys.LOCATION_CREATED, location);
    }

    public static LocationEvent forUpdated(Location location) {
        return of(RoutingKeys.format(RoutingKeys.LOCATION_UPDATED,
            Map.of("locationId", String.valueOf(location.getId()))), location);
    }
}
