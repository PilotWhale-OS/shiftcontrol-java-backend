package at.shiftcontrol.shiftservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public abstract class ApplicationEvent {
    public abstract String getRoutingKey();
}
