package at.shiftcontrol.shiftservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class BaseEvent {
    public abstract String getRoutingKey();
}
