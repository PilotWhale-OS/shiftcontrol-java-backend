package at.shiftcontrol.shiftservice.event.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.TradePart;

@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class TradeEvent extends BaseEvent {
    @JsonIgnore
    private final String routingKey;

    private final TradePart trade;

    public static TradeEvent of(AssignmentSwitchRequest trade, String routingKey) {
        return new TradeEvent(routingKey, TradePart.of(trade));
    }
}
