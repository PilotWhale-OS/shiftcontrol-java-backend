package at.shiftcontrol.lib.event.events;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.event.EventType;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.PositionSlotPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class PositionSlotVolunteerEvent extends PositionSlotEvent {
    private final String volunteerId;

    @JsonCreator
    public PositionSlotVolunteerEvent(
        @JsonProperty("eventType") EventType eventType,
        @JsonProperty("routingKey") String routingKey,
        @JsonProperty("positionSlot") PositionSlotPart positionSlot,
        @JsonProperty("volunteerId") String volunteerId) {
        super(eventType, routingKey, positionSlot);
        this.volunteerId = volunteerId;
    }

    public static PositionSlotVolunteerEvent ofInternal(EventType eventType, String routingKey, PositionSlot positionSlot, String volunteerId) {
        return new PositionSlotVolunteerEvent(eventType, routingKey, PositionSlotPart.of(positionSlot), volunteerId);
    }

    public static PositionSlotVolunteerEvent positionSlotJoined(PositionSlot positionSlot, String volunteerId) {
        return ofInternal(EventType.POSITIONSLOT_JOINED,
                RoutingKeys.format(RoutingKeys.POSITIONSLOT_JOINED,
                Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                    "volunteerId", volunteerId)),
            positionSlot, volunteerId);
    }

    public static PositionSlotVolunteerEvent positionSlotLeft(PositionSlot positionSlot, String volunteerId) {
            return ofInternal(EventType.POSITIONSLOT_LEFT,
                    RoutingKeys.format(RoutingKeys.POSITIONSLOT_LEFT,
                Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                    "volunteerId", volunteerId)),
            positionSlot, volunteerId);
    }

    public static PositionSlotVolunteerEvent positionSlotJoinRequestDenied(PositionSlot positionSlot, String volunteerId) {
        return ofInternal(EventType.POSITIONSLOT_REQUEST_JOIN_DECLINED,
                RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_JOIN_DECLINED,
                Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                    "volunteerId", volunteerId)),
            positionSlot, volunteerId);
    }

    public static PositionSlotVolunteerEvent positionSlotJoinRequestCreated(PositionSlot positionSlot, String volunteerId) {
        return ofInternal(EventType.POSITIONSLOT_REQUEST_JOIN,
                RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_JOIN,
                Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                    "volunteerId", volunteerId)),
            positionSlot, volunteerId);
    }

    public static PositionSlotVolunteerEvent positionSlotJoinRequestWithdrawn(PositionSlot positionSlot, String volunteerId) {
        return ofInternal(EventType.POSITIONSLOT_REQUEST_JOIN_WITHDRAW,
                RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_JOIN_WITHDRAW,
                Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                    "volunteerId", volunteerId)),
            positionSlot, volunteerId);
    }

    public static PositionSlotVolunteerEvent positionSlotJoinRequestAccepted(PositionSlot positionSlot, String volunteerId) {
        return ofInternal(EventType.POSITIONSLOT_REQUEST_JOIN_ACCEPTED,
                RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_JOIN_ACCEPTED,
                Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                    "volunteerId", volunteerId)),
            positionSlot, volunteerId);
    }

    public static PositionSlotVolunteerEvent positionSlotJoinRequestDeclined(PositionSlot positionSlot, String volunteerId) {
        return ofInternal(EventType.POSITIONSLOT_REQUEST_JOIN_DECLINED,
                RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_JOIN_DECLINED,
                Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                    "volunteerId", volunteerId)),
            positionSlot, volunteerId);
    }

    public static PositionSlotVolunteerEvent positionSlotRequestLeave(PositionSlot positionSlot, String volunteerId) {
        return ofInternal(EventType.POSITIONSLOT_REQUEST_LEAVE,
                RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE,
                Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                    "volunteerId", volunteerId)),
            positionSlot, volunteerId);
    }

    public static PositionSlotVolunteerEvent positionSlotLeaveRequestWithdrawn(PositionSlot positionSlot, String volunteerId) {
        return ofInternal(EventType.POSITIONSLOT_REQUEST_LEAVE_WITHDRAW,
                RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_WITHDRAW,
                Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                    "volunteerId", volunteerId)),
            positionSlot, volunteerId);
    }

    public static PositionSlotVolunteerEvent positionSlotLeaveRequestAccepted(PositionSlot positionSlot, String volunteerId) {
        return ofInternal(EventType.POSITIONSLOT_REQUEST_LEAVE_ACCEPTED,
                RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_ACCEPTED,
                Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                    "volunteerId", volunteerId)),
            positionSlot, volunteerId);
    }

    public static PositionSlotVolunteerEvent positionSlotLeaveRequestDeclined(PositionSlot positionSlot, String volunteerId) {
        return ofInternal(EventType.POSITIONSLOT_REQUEST_LEAVE_DECLINED,
                RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_DECLINED,
                Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                    "volunteerId", volunteerId)),
            positionSlot, volunteerId);
    }
}
