package at.shiftcontrol.lib.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.EventType;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.RolePart;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleEvent extends BaseEvent {
    private final RolePart role;

    public RoleEvent(EventType eventType, String routingKey, RolePart role) {
        super(eventType, routingKey);
        this.role = role;
    }

    public static RoleEvent ofInternal(EventType eventType, String routingKey, Role role) {
        return new RoleEvent(eventType, routingKey, RolePart.of(role));
    }

    public static RoleEvent roleCreated(Role role) {
        return ofInternal(EventType.ROLE_CREATED, RoutingKeys.ROLE_CREATED, role);
    }

    public static RoleEvent roleUpdated(Role role) {
        return ofInternal(EventType.ROLE_UPDATED,
            RoutingKeys.format(RoutingKeys.ROLE_UPDATED,
            java.util.Map.of("roleId", String.valueOf(role.getId()))), role);
    }

    public static RoleEvent roleDeleted(Role role) {
        return ofInternal(EventType.ROLE_DELETED,
            RoutingKeys.format(RoutingKeys.ROLE_DELETED,
            java.util.Map.of("roleId", String.valueOf(role.getId()))), role);
    }
}

