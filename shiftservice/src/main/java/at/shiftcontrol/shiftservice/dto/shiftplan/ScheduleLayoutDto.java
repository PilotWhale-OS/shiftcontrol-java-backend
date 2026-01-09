package at.shiftcontrol.shiftservice.dto.shiftplan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.lib.dto.location.LocationDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleLayoutDto {
    @NotNull
    @Valid
    private LocationDto location;

    @NotNull
    @Min(0)
    private int requiredShiftColumns;
}
