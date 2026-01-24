package at.shiftcontrol.lib.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.RewardPointsShareToken;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.EventType;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.shiftservice.event.events.parts.RewardPointsShareTokenPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class RewardPointsShareTokenEvent extends BaseEvent {
    private final RewardPointsShareTokenPart rewardPointsShareTokenPart;

    public RewardPointsShareTokenEvent(EventType eventType, String routingKey, RewardPointsShareTokenPart rewardPointsShareTokenPart) {
        super(eventType, );
        this.rewardPointsShareTokenPart = rewardPointsShareTokenPart;
    }

    public static RewardPointsShareTokenEvent ofInternal(EventType eventType, String routingKey, RewardPointsShareToken shareToken) {
        return new RewardPointsShareTokenEvent(routingKey, RewardPointsShareTokenPart.of(shareToken));
    }

    public static RewardPointsShareTokenEvent shareTokenCreated(RewardPointsShareToken shareToken) {
        return ofInternal(RoutingKeys.REWARDPOINTS_SHARETOKEN_CREATED, shareToken);
    }
}
