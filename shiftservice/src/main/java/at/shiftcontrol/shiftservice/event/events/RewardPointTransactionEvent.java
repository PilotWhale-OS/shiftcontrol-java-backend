package at.shiftcontrol.shiftservice.event.events;

import at.shiftcontrol.shiftservice.entity.RewardPointsTransaction;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.RewardPointTransactionPart;
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
