package at.shiftcontrol.shiftservice.event.events.parts;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import at.shiftcontrol.lib.entity.PositionSlot;

@Data
@Builder
public class PositionSlotPart {
    private long positionSlotId;
    private String positionSlotName;
    private String positionSlotDescription;

    @NonNull
    public static PositionSlotPart of(@NonNull PositionSlot positionSlot) {
        return PositionSlotPart.builder()
                .positionSlotId(positionSlot.getId())
                .positionSlotName(positionSlot.getName())
                .positionSlotDescription(positionSlot.getDescription())
                .build();
    }
}
