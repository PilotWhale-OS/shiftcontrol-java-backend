package at.shiftcontrol.shiftservice.event;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.event.BaseEvent;

@Data
@Builder
@RequiredArgsConstructor
public class ApplicationEventWrapper {
    private final String actingUserId;
    private final String traceId;
    private final Instant timestamp;
//     private final EventType eventType;
    private final BaseEvent event;
}
