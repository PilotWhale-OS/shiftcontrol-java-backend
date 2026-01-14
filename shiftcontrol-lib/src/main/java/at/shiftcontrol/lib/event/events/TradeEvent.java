package at.shiftcontrol.lib.event.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.events.parts.TradePart;

@Data
@EqualsAndHashCode(callSuper = true)
public class TradeEvent extends BaseEvent {
    private final TradePart trade;

    @JsonCreator
    public TradeEvent(
        @JsonProperty("routingKey") String routingKey,
        @JsonProperty("trade") TradePart trade) {
        super(routingKey);
        this.trade = trade;
    }

    public static TradeEvent of(String routingKey, AssignmentSwitchRequest trade) {
        return new TradeEvent(routingKey, TradePart.of(trade));
    }
}
