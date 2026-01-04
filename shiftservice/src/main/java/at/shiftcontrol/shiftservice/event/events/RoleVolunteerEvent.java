package at.shiftcontrol.shiftservice.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.shiftservice.entity.role.Role;
import at.shiftcontrol.shiftservice.event.events.parts.RolePart;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleVolunteerEvent extends RoleEvent {
    private final String volunteerId;

    public RoleVolunteerEvent(String routingKey, RolePart role, String volunteerId) {
        super(role, routingKey);
        this.volunteerId = volunteerId;
    }

    public static RoleVolunteerEvent of(String routingKey, Role role, String volunteerId) {
        return new RoleVolunteerEvent(routingKey, RolePart.of(role), volunteerId);
    }
}
