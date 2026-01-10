package at.shiftcontrol.shiftservice.sync.pretalx;

import java.util.List;

import lombok.Value;

@Value
public class PretalxApiKeyData {
    String apiKey;
    List<String> eventSlugs;
}
