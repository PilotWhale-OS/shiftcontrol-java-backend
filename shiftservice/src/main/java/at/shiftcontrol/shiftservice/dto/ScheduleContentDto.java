package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

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
