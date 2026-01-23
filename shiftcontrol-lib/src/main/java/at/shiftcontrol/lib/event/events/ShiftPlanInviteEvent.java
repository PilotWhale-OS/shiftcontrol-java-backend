package at.shiftcontrol.lib.event.events;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.ShiftPlanInvite;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.InvitePart;
import at.shiftcontrol.lib.event.events.parts.ShiftPlanPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftPlanInviteEvent extends ShiftPlanEvent {
    private final InvitePart invite;

    public ShiftPlanInviteEvent(String routingKey, ShiftPlanPart shiftPlan, InvitePart invite) {
        super(routingKey, shiftPlan);
        this.invite = invite;
    }

    public static ShiftPlanInviteEvent ofInternal(String routingKey, ShiftPlanInvite invite) {
        return new ShiftPlanInviteEvent(routingKey, ShiftPlanPart.of(invite.getShiftPlan()), InvitePart.of(invite));
    }

    public static ShiftPlanInviteEvent inviteCreated(ShiftPlanInvite invite) {
        return ofInternal(RoutingKeys.format(RoutingKeys.SHIFTPLAN_INVITE_CREATED,
            Map.of("shiftPlanId", String.valueOf(invite.getShiftPlan().getId()),
                "inviteId", String.valueOf(invite.getId()))), invite);
    }

    public static ShiftPlanInviteEvent inviteDeleted(ShiftPlanInvite invite) {
        return ofInternal(RoutingKeys.format(RoutingKeys.SHIFTPLAN_INVITE_DELETED,
            Map.of("shiftPlanId", String.valueOf(invite.getShiftPlan().getId()),
                "inviteId", String.valueOf(invite.getId()))), invite);
    }

    public static ShiftPlanInviteEvent inviteRevoked(ShiftPlanInvite invite) {
        return ofInternal(RoutingKeys.format(RoutingKeys.SHIFTPLAN_INVITE_REVOKED,
            Map.of("shiftPlanId", String.valueOf(invite.getShiftPlan().getId()),
                "inviteId", String.valueOf(invite.getId()))), invite);
    }
}
