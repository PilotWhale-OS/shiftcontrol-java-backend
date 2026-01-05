package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.event.events.parts.TradePart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class TradeEventTest {

    @Test
    void of() {
        AssignmentSwitchRequest trade = mock(AssignmentSwitchRequest.class);
        String routingKey = "routingKey";

        TradePart tradePart = mock(TradePart.class);
        try (var tradePartMock = org.mockito.Mockito.mockStatic(TradePart.class)) {
            tradePartMock.when(() -> TradePart.of(trade)).thenReturn(tradePart);

            TradeEvent tradeEvent = TradeEvent.of(trade, routingKey);

            assertEquals(routingKey, tradeEvent.getRoutingKey());
            assertEquals(tradePart, tradeEvent.getTrade());
        }
    }
}

