package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.event.events.RoleVolunteerEvent;
import at.shiftcontrol.lib.event.events.parts.RolePart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RoleVolunteerEventTest {

    @Test
    void ofInternal() {
        String routingKey = "routingKey";
        Role role = new Role();
        ShiftPlan shiftPlan = new ShiftPlan();
        role.setShiftPlan(shiftPlan);
        Event event = new Event();
        shiftPlan.setEvent(event);
        String volunteerId = "volunteerId";

        RolePart rolePart = mock(RolePart.class);
        try (var rolePartMock = org.mockito.Mockito.mockStatic(RolePart.class)) {
            rolePartMock.when(() -> RolePart.of(role)).thenReturn(rolePart);

            RoleVolunteerEvent roleVolunteerEvent = RoleVolunteerEvent.ofInternal(null, routingKey, role, volunteerId);

            assertEquals(routingKey, roleVolunteerEvent.getRoutingKey());
            assertEquals(rolePart, roleVolunteerEvent.getRole());
            assertEquals(volunteerId, roleVolunteerEvent.getVolunteerId());
        }
    }
}

