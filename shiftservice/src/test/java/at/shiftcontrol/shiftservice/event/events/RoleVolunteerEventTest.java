package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.event.events.RoleVolunteerEvent;
import at.shiftcontrol.lib.event.events.parts.RolePart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RoleVolunteerEventTest {

    @Test
    void of() {
        String routingKey = "routingKey";
        Role role = mock(Role.class);
        String volunteerId = "volunteerId";

        RolePart rolePart = mock(RolePart.class);
        try (var rolePartMock = org.mockito.Mockito.mockStatic(RolePart.class)) {
            rolePartMock.when(() -> RolePart.of(role)).thenReturn(rolePart);

            RoleVolunteerEvent roleVolunteerEvent = RoleVolunteerEvent.of(routingKey, role, volunteerId);

            assertEquals(routingKey, roleVolunteerEvent.getRoutingKey());
            assertEquals(rolePart, roleVolunteerEvent.getRole());
            assertEquals(volunteerId, roleVolunteerEvent.getVolunteerId());
        }
    }
}

