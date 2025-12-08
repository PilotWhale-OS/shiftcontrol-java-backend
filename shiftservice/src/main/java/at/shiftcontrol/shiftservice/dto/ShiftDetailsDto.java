package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftDetailsDto {
    @NotNull
    private ShiftDto shift;
    @NotNull
    private UserShiftPreferenceDto preference;
    // TODO additional info needed for trade/auction?
}
