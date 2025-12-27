package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.shift.ShiftColumnDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationScheduleDto {
    @NotNull
    private LocationDto location;

    @NotNull
    private Collection<ActivityDto> activities;

    @NotNull
    private int requiredShiftColumns;

    @NotNull
    private Collection<ShiftColumnDto> shiftColumns;
}
