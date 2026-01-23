package at.shiftcontrol.lib.event.events;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.AssignmentPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class AssignmentEvent extends BaseEvent {
    private final AssignmentPart assignment;

    @JsonCreator
    public AssignmentEvent(
        @JsonProperty("routingKey") String routingKey,
        @JsonProperty("assignment") AssignmentPart assignment) {
        super(routingKey);
        this.assignment = assignment;
    }

    public static AssignmentEvent of(String routingKey, Assignment assignment) {
        return new AssignmentEvent(routingKey, AssignmentPart.of(assignment));
    }

    public static AssignmentEvent forAuctionClaimed(Assignment auction, Assignment oldAuction, String oldVolunteerId) {
        return of(RoutingKeys.format(RoutingKeys.AUCTION_CLAIMED, Map.of(
            "positionSlotId", String.valueOf(oldAuction.getPositionSlot().getId()),
            "oldVolunteerId", oldVolunteerId)), auction
        );
    }

    public static AssignmentEvent forAuctionCreated(Assignment auction) {
        return of(
            RoutingKeys.format(RoutingKeys.AUCTION_CREATED,
                Map.of("positionSlotId", String.valueOf(auction.getPositionSlot().getId()))
            ), auction);
    }

    public static AssignmentEvent forAuctionCanceled(Assignment auction) {
        return of(
            RoutingKeys.format(RoutingKeys.AUCTION_CANCELED,
                Map.of("positionSlotId", String.valueOf(auction.getPositionSlot().getId()))
            ), auction);
    }
}
