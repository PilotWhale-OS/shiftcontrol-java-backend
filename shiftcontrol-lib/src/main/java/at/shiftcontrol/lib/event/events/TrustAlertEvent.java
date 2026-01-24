package at.shiftcontrol.lib.event.events;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.TrustAlert;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.EventType;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.TrustAlertPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class TrustAlertEvent extends BaseEvent {
    private final TrustAlertPart trustAlertPart;

    public TrustAlertEvent(EventType eventType, String routingKey, TrustAlertPart trustAlertPart) {
        super(eventType, routingKey);
        this.trustAlertPart = trustAlertPart;
    }

    private static TrustAlertEvent of(EventType eventType, String routingKey, TrustAlert trustAlertPart) {
        return new TrustAlertEvent(eventType, routingKey, TrustAlertPart.of(trustAlertPart));
    }

    public static TrustAlertEvent alertReceived(TrustAlert trustAlert) {
        return of(EventType.TRUST_ALERT_RECEIVED,
            RoutingKeys.format(RoutingKeys.TRUST_ALERT_RECEIVED,
                Map.of("alertId", String.valueOf(trustAlert.getId()))
            ), trustAlert
        );
    }
}
