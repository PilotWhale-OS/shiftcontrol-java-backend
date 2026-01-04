package at.shiftcontrol.shiftservice.event.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.entity.role.Role;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.RolePart;

@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class RoleEvent extends BaseEvent {
    private final RolePart role;
    @JsonIgnore
    private final String routingKey;


    public static RoleEvent of(Role role, String routingKey) {
        return new RoleEvent(RolePart.of(role), routingKey);
    }
}

