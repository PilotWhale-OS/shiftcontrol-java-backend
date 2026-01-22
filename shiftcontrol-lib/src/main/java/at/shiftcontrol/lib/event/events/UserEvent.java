package at.shiftcontrol.lib.event.events;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.VolunteerPart;
import static at.shiftcontrol.lib.event.RoutingKeys.USERS_EVENT_LOCK;
import static at.shiftcontrol.lib.event.RoutingKeys.USERS_EVENT_UNLOCK;
import static at.shiftcontrol.lib.event.RoutingKeys.USERS_EVENT_UPDATE;
import static at.shiftcontrol.lib.event.RoutingKeys.USERS_PLAN_UPDATE;
import static at.shiftcontrol.lib.event.RoutingKeys.USERS_RESET;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserEvent extends BaseEvent {
    private final VolunteerPart volunteer;

    public UserEvent(String routingKey, VolunteerPart volunteer) {
        super(routingKey);
        this.volunteer = volunteer;
    }

    public static UserEvent eventUpdate(Volunteer volunteer) {
        return new UserEvent(RoutingKeys.format(USERS_EVENT_UPDATE, Map.of("userId", volunteer.getId())), VolunteerPart.of(volunteer));
    }

    public static UserEvent planUpdate(Volunteer volunteer, String shiftPlanId) {
        return new UserEvent(RoutingKeys.format(USERS_PLAN_UPDATE,
            Map.of("shiftPlanId", shiftPlanId, "userId", volunteer.getId())),
            VolunteerPart.of(volunteer));
    }

    public static UserEvent lock(Volunteer volunteer) {
        return new UserEvent(RoutingKeys.format(USERS_EVENT_LOCK, Map.of("userId", volunteer.getId())), VolunteerPart.of(volunteer));
    }

    public static UserEvent unlock(Volunteer volunteer) {
        return new UserEvent(RoutingKeys.format(USERS_EVENT_UNLOCK, Map.of("userId", volunteer.getId())), VolunteerPart.of(volunteer));
    }

    public static UserEvent reset(Volunteer volunteer) {
        return new UserEvent(RoutingKeys.format(USERS_RESET, Map.of("userId", volunteer.getId())), VolunteerPart.of(volunteer));
    }
}
