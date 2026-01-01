package at.shiftcontrol.shiftservice.event;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class ApplicationEventWrapper {
    private final String actingUserId;
    private final Instant timestamp;
    private final ApplicationEvent event;
}
