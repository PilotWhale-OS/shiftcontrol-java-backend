package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.Location;
import at.shiftcontrol.shiftservice.event.events.parts.LocationPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class LocationEventTest {

    @Test
    void of() {
        Location location = mock(Location.class);
        String routingKey = "routingKey";

        LocationPart locationPart = mock(LocationPart.class);
        try (var locationPartMock = org.mockito.Mockito.mockStatic(LocationPart.class)) {
            locationPartMock.when(() -> LocationPart.of(location)).thenReturn(locationPart);

            LocationEvent locationEvent = LocationEvent.of(location, routingKey);

            assertEquals(locationPart, locationEvent.getLocation());
            assertEquals(routingKey, locationEvent.getRoutingKey());
        }
    }
}

