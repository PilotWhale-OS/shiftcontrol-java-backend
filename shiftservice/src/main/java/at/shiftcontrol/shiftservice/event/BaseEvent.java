package at.shiftcontrol.shiftservice.event;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public abstract class BaseEvent {
    @JsonIgnore
    private final String routingKey;

    private String actingUserId;
    private String traceId;
    private Instant timestamp = Instant.now();
}
