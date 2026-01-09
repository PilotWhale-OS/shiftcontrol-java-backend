package at.shiftcontrol.shiftservice.sync.pretalx;

import java.util.Map;

import lombok.Value;

@Value
public class PreTalxConfig {
    Map<String, String> eventToApiKeyMap;
}
