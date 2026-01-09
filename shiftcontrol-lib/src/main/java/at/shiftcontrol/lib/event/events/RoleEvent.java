package at.shiftcontrol.lib.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.events.parts.RolePart;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleEvent extends BaseEvent {
    private final RolePart role;

    public RoleEvent(String routingKey, RolePart role) {
        super(routingKey);
        this.role = role;
    }

    public static RoleEvent of(String routingKey, Role role) {
        return new RoleEvent(routingKey, RolePart.of(role));
    }
}

