package at.shiftcontrol.lib.event.events;

import java.util.Collection;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.RolePart;
import at.shiftcontrol.lib.event.events.parts.VolunteerPart;
import static at.shiftcontrol.lib.event.RoutingKeys.USERS_PLAN_BULK_ADD;
import static at.shiftcontrol.lib.event.RoutingKeys.USERS_PLAN_BULK_REMOVE;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserPlanBulkEvent extends BaseEvent {
    private final Collection<VolunteerPart> volunteers;
    private final Collection<RolePart> roles;

    public UserPlanBulkEvent(String routingKey, Collection<VolunteerPart> volunteers, Collection<RolePart> roles) {
        super(routingKey);
        this.volunteers = volunteers;
        this.roles = roles;
    }

    public static UserPlanBulkEvent add(Collection<Volunteer> volunteers, Collection<Role> plans, String shiftPlanId) {
        return new UserPlanBulkEvent(RoutingKeys.format(USERS_PLAN_BULK_ADD, Map.of("shiftPlanId", shiftPlanId)),
            VolunteerPart.of(volunteers), RolePart.of(plans));
    }

    public static UserPlanBulkEvent remove(Collection<Volunteer> volunteers, Collection<Role> plans, String shiftPlanId) {
        return new UserPlanBulkEvent(RoutingKeys.format(USERS_PLAN_BULK_REMOVE, Map.of("shiftPlanId", shiftPlanId)),
            VolunteerPart.of(volunteers), RolePart.of(plans));
    }
}
