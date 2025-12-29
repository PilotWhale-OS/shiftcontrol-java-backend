package at.shiftcontrol.shiftservice.dto.shiftplan;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.ActivityDto;
import at.shiftcontrol.shiftservice.dto.LocationDto;
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
public class ScheduleContentDto {
    @NotNull
    private LocationDto location;

    @NotNull
    private Collection<ActivityDto> activities;

    @NotNull
    private Collection<ShiftColumnDto> shiftColumns;
}
