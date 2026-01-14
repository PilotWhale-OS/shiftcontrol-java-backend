package at.shiftcontrol.shiftservice.sync.pretalx;

import java.util.Map;

import lombok.Value;

@Value
public class PretalxConfig {
    Map<String, String> eventToApiKeyMap;
}
