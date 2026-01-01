package at.shiftcontrol.shiftservice.dto.positionslot;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PositionSlotPreferenceDto {
    private int preferenceValue;
}
