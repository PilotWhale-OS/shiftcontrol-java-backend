package at.shiftcontrol.shiftservice.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class BaseEvent {
    @JsonIgnore
    private final String routingKey;
}
