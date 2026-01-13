package at.shiftcontrol.lib.event.events;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.RoutingKeys;

@Data
@EqualsAndHashCode(callSuper = true)
public class PretalxApiKeyInvalidEvent extends BaseEvent {
    private final String apiKey;

    public PretalxApiKeyInvalidEvent(String apiKey) {
        super(RoutingKeys.format(RoutingKeys.PRETALX_API_KEY_INVALID, Map.of("apiKey", apiKey)));
        this.apiKey = apiKey;
    }
}
