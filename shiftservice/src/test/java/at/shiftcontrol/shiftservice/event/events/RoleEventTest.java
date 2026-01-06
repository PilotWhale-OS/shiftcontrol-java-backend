package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.role.Role;
import at.shiftcontrol.shiftservice.event.events.parts.RolePart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RoleEventTest {

    @Test
    void of() {
        Role role = mock(Role.class);
        String routingKey = "routingKey";

        RolePart rolePart = mock(RolePart.class);
        try (var rolePartMock = org.mockito.Mockito.mockStatic(RolePart.class)) {
            rolePartMock.when(() -> RolePart.of(role)).thenReturn(rolePart);

            RoleEvent roleEvent = RoleEvent.of(routingKey, role);

            assertEquals(rolePart, roleEvent.getRole());
            assertEquals(routingKey, roleEvent.getRoutingKey());
        }
    }
}

