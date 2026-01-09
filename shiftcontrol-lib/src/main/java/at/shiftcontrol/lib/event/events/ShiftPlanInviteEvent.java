package at.shiftcontrol.lib.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.ShiftPlanInvite;
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

    public static ShiftPlanInviteEvent of(String routingKey, ShiftPlanInvite invite) {
        return new ShiftPlanInviteEvent(routingKey, ShiftPlanPart.of(invite.getShiftPlan()), InvitePart.of(invite));
    }
}
