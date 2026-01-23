package at.shiftcontrol.lib.event.events;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.RolePart;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleVolunteerEvent extends RoleEvent {
    private final String volunteerId;

    public RoleVolunteerEvent(String routingKey, RolePart role, String volunteerId) {
        super(routingKey, role);
        this.volunteerId = volunteerId;
    }

    public static RoleVolunteerEvent ofInternal(String routingKey, Role role, String volunteerId) {
        return new RoleVolunteerEvent(routingKey, RolePart.of(role), volunteerId);
    }

    public static RoleVolunteerEvent roleAssigned(Role role, String volunteerId) {
        return ofInternal(RoutingKeys.format(RoutingKeys.ROLE_ASSIGNED, Map.of(
                "roleId", String.valueOf(role.getId()),
                "volunteerId", volunteerId)),
            role, volunteerId);
    }

    public static RoleVolunteerEvent roleUnassigned(Role role, String volunteerId) {
        return ofInternal(RoutingKeys.format(RoutingKeys.ROLE_UNASSIGNED, Map.of(
                "roleId", String.valueOf(role.getId()),
                "volunteerId", volunteerId)),
            role, volunteerId);
    }
}
