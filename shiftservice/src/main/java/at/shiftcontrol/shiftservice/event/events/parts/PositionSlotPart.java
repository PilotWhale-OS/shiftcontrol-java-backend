package at.shiftcontrol.shiftservice.event.events.parts;

import at.shiftcontrol.shiftservice.entity.PositionSlot;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PositionSlotPart {
    private long positionSlotId;
    private String positionSlotName;
    private String positionSlotDescription;

    public static PositionSlotPart of(PositionSlot positionSlot) {
        return PositionSlotPart.builder()
                .positionSlotId(positionSlot.getId())
                .positionSlotName(positionSlot.getName())
                .positionSlotDescription(positionSlot.getDescription())
                .build();
    }
}
