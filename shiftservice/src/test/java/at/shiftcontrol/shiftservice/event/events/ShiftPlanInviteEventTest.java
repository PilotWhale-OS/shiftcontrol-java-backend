package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.ShiftPlanInvite;
import at.shiftcontrol.shiftservice.event.events.parts.InvitePart;
import at.shiftcontrol.shiftservice.event.events.parts.ShiftPlanPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShiftPlanInviteEventTest {

    @Test
    void of() {
        String routingKey = "routingKey";
        ShiftPlanInvite invite = mock(ShiftPlanInvite.class);
        ShiftPlan shiftPlan = mock(ShiftPlan.class);
        when(invite.getShiftPlan()).thenReturn(shiftPlan);


        ShiftPlanPart shiftPlanPart = mock(ShiftPlanPart.class);
        InvitePart invitePart = mock(InvitePart.class);
        try (var shiftPlanPartMock = org.mockito.Mockito.mockStatic(ShiftPlanPart.class);
             var invitePartMock = org.mockito.Mockito.mockStatic(InvitePart.class)) {
            shiftPlanPartMock.when(() -> ShiftPlanPart.of(shiftPlan)).thenReturn(shiftPlanPart);
            invitePartMock.when(() -> InvitePart.of(invite)).thenReturn(invitePart);

            ShiftPlanInviteEvent shiftPlanInviteEvent = ShiftPlanInviteEvent.of(routingKey, invite);

            assertEquals(routingKey, shiftPlanInviteEvent.getRoutingKey());
            assertEquals(shiftPlanPart, shiftPlanInviteEvent.getShiftPlan());
            assertEquals(invitePart, shiftPlanInviteEvent.getInvite());
        }
    }
}

