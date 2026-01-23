package at.shiftcontrol.lib.event.events;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Location;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.LocationPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocationEvent extends BaseEvent {
    private final LocationPart location;

    public LocationEvent(String routingKey, LocationPart location) {
        super(routingKey);
        this.location = location;
    }

    public static LocationEvent ofInternal(String routingKey, Location location) {
        return new LocationEvent(routingKey, LocationPart.of(location));
    }

    public static LocationEvent locationCreated(Location location) {
        return ofInternal(RoutingKeys.LOCATION_CREATED, location);
    }

    public static LocationEvent locationUpdated(Location location) {
        return ofInternal(RoutingKeys.format(RoutingKeys.LOCATION_UPDATED,
            Map.of("locationId", String.valueOf(location.getId()))), location);
    }

    public static LocationEvent locationDeleted(Location location) {
        return ofInternal(RoutingKeys.format(RoutingKeys.LOCATION_DELETED,
            Map.of("locationId", String.valueOf(location.getId()))), location);
    }
}
