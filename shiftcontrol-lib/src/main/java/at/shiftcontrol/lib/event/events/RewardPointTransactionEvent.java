package at.shiftcontrol.lib.event.events;

import at.shiftcontrol.lib.entity.RewardPointsTransaction;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.events.parts.RewardPointsTransactionPart;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RewardPointTransactionEvent extends BaseEvent {
    private final RewardPointsTransactionPart rewardPointsTransactionPart;

    public RewardPointTransactionEvent(String routingKey, RewardPointsTransactionPart rewardPointsTransactionPart) {
        super(routingKey);
        this.rewardPointsTransactionPart = rewardPointsTransactionPart;
    }

    public static RewardPointTransactionEvent of(String routingKey, RewardPointsTransaction transaction) {
        return new RewardPointTransactionEvent(routingKey, RewardPointsTransactionPart.of(transaction));
    }
}
