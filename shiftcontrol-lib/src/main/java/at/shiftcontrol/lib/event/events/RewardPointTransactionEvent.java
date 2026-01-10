package at.shiftcontrol.lib.event.events;

import at.shiftcontrol.lib.entity.RewardPointsTransaction;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.events.parts.RewardPointTransactionPart;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RewardPointTransactionEvent extends BaseEvent {
    private final RewardPointTransactionPart rewardPointTransactionPart;

    public RewardPointTransactionEvent(String routingKey, RewardPointTransactionPart rewardPointTransactionPart) {
        super(routingKey);
        this.rewardPointTransactionPart = rewardPointTransactionPart;
    }

    public static RewardPointTransactionEvent of(String routingKey, RewardPointsTransaction transaction) {
        return new RewardPointTransactionEvent(routingKey, RewardPointTransactionPart.of(transaction));
    }
}
