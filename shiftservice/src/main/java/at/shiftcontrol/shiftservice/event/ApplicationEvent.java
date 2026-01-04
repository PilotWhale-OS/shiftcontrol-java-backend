package at.shiftcontrol.shiftservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class ApplicationEvent {
    public abstract String getRoutingKey();
}
