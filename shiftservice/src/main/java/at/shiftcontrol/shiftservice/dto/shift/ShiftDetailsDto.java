package at.shiftcontrol.shiftservice.dto.shift;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotPreferenceDto;

@Data
@Builder
public class ShiftDetailsDto {
    @NotNull
    @Valid
    private ShiftDto shift;

    @NotNull
    @Valid
    private PositionSlotPreferenceDto preference;
    // TODO additional info needed for trade/auction?
}
