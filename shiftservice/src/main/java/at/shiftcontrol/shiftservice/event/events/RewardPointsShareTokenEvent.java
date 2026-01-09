package at.shiftcontrol.shiftservice.event.events;

import at.shiftcontrol.shiftservice.entity.RewardPointsShareToken;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.RewardPointsShareTokenPart;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RewardPointsShareTokenEvent extends BaseEvent {
    private final RewardPointsShareTokenPart rewardPointsShareTokenPart;

    public RewardPointsShareTokenEvent(String routingKey, RewardPointsShareTokenPart rewardPointsShareTokenPart) {
        super(routingKey);
        this.rewardPointsShareTokenPart = rewardPointsShareTokenPart;
    }

    public static RewardPointsShareTokenEvent of(String routingKey, RewardPointsShareToken shareToken) {
        return new RewardPointsShareTokenEvent(routingKey, RewardPointsShareTokenPart.of(shareToken));
    }
}
