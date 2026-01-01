package at.shiftcontrol.shiftservice.dto.shift;

import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotPreferenceDto;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftDetailsDto {
    @NotNull
    private ShiftDto shift;
    @NotNull
    private PositionSlotPreferenceDto preference;
    // TODO additional info needed for trade/auction?
}
