package at.shiftcontrol.lib.event;

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

    public BaseEvent withActingUserId(String actingUserId) {
        this.actingUserId = actingUserId;
        return this;
    }
}
