package at.shiftcontrol.shiftservice.event.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.ShiftPart;

@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class ShiftEvent extends BaseEvent {
    private final ShiftPart shift;
    @JsonIgnore
    private final String routingKey;

    public static ShiftEvent of(Shift shift, String routingKey) {
        return new ShiftEvent(ShiftPart.of(shift), routingKey);
    }
}
