package at.shiftcontrol.shiftservice.dto.shiftplan;

import at.shiftcontrol.shiftservice.dto.LocationDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleLayoutDto {
    @NotNull
    private LocationDto location;

    @NotNull
    private int requiredShiftColumns;
}
