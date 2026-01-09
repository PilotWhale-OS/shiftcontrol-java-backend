package at.shiftcontrol.shiftservice.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.TradePart;

@Data
@EqualsAndHashCode(callSuper = true)
public class TradeEvent extends BaseEvent {
    private final TradePart trade;

    public TradeEvent(String routingKey, TradePart trade) {
        super(routingKey);
        this.trade = trade;
    }

    public static TradeEvent of(String routingKey, AssignmentSwitchRequest trade) {
        return new TradeEvent(routingKey, TradePart.of(trade));
    }
}
