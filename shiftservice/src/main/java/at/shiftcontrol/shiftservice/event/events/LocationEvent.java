package at.shiftcontrol.shiftservice.event.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.entity.Location;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.LocationPart;

@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class LocationEvent extends BaseEvent {
    private final LocationPart location;

    @JsonIgnore
    private final String routingKey;

    public static LocationEvent of(Location location, String routingKey) {
        return new LocationEvent(LocationPart.of(location), routingKey);
    }
}
