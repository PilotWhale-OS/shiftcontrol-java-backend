package at.shiftcontrol.shiftservice.dto.shiftplan;

import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.lib.dto.location.LocationDto;
import at.shiftcontrol.shiftservice.dto.activity.ActivityDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftColumnDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleContentDto {
    @NotNull
    @Valid
    private LocationDto location;

    @NotNull
    @Valid
    private Collection<ActivityDto> activities;

    @NotNull
    @Valid
    private Collection<ShiftColumnDto> shiftColumns;
}
