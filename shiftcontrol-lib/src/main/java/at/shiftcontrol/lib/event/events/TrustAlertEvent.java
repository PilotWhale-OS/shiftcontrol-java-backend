package at.shiftcontrol.lib.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.TrustAlert;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.events.parts.TrustAlertPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class TrustAlertEvent extends BaseEvent {
    private final TrustAlertPart trustAlertPart;

    public TrustAlertEvent(String routingKey, TrustAlertPart trustAlertPart) {
        super(routingKey);
        this.trustAlertPart = trustAlertPart;
    }

    public static TrustAlertEvent of(String routingKey, TrustAlert trustAlertPart) {
        return new TrustAlertEvent(routingKey, TrustAlertPart.of(trustAlertPart));
    }
}
