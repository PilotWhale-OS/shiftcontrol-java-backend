package at.shiftcontrol.shiftservice.event.events.parts;

import java.time.Instant;
import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import at.shiftcontrol.shiftservice.entity.Shift;

@Data
@AllArgsConstructor
public class ShiftPart {
    @NotNull
    private String id;
    @NotNull
    private String name;
    private String shortDescription;
    private String longDescription;
    @NotNull
    private Instant startTime;
    @NotNull
    private Instant endTime;
    private long relatedActivityId;
    @NotNull
    private Collection<PositionSlotPart> positionSlots;
    @NotNull
    private long locationId;

    @NonNull
    public static ShiftPart of(@NonNull Shift shift) {
        return new ShiftPart(
            String.valueOf(shift.getId()),
            shift.getName(),
            shift.getShortDescription(),
            shift.getLongDescription(),
            shift.getStartTime(),
            shift.getEndTime(),
            shift.getRelatedActivity() != null ? shift.getRelatedActivity().getId() : 0,
            shift.getSlots().stream().map(PositionSlotPart::of).toList(),
            shift.getLocation() != null ? shift.getLocation().getId() : 0
        );
    }
}
