package at.shiftcontrol.shiftservice.dto;

import java.time.LocalDate;
import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftPlanScheduleFilterValuesDto {
    @NotNull
    private Collection<LocationDto> locations;
    @NotNull
    private Collection<RoleDto> roles;

    private LocalDate firstDate;
    private LocalDate lastDate;
}
