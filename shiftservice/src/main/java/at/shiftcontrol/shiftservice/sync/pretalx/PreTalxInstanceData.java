package at.shiftcontrol.shiftservice.sync.pretalx;

import java.util.List;

public class PreTalxInstanceData {
    List<PreTalxApiKeyData> apiKeys;

    public static class PreTalxApiKeyData {
        String apiKey;
        List<String> eventSlugs;
    }
}
