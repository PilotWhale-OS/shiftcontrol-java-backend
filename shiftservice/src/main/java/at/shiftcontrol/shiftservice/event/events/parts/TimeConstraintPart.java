package at.shiftcontrol.shiftservice.event.events.parts;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import at.shiftcontrol.shiftservice.entity.TimeConstraint;
import at.shiftcontrol.shiftservice.type.TimeConstraintType;

@AllArgsConstructor
@Data
public class TimeConstraintPart {
    @NotNull
    private String id;
    @NotNull
    private TimeConstraintType type;
    @NotNull
    private Instant from;
    @NotNull
    private Instant to;

    public static TimeConstraintPart of(TimeConstraint timeConstraint) {
        return new TimeConstraintPart(
            String.valueOf(timeConstraint.getId()),
            timeConstraint.getType(),
            timeConstraint.getStartTime(),
            timeConstraint.getEndTime()
        );
    }
}
