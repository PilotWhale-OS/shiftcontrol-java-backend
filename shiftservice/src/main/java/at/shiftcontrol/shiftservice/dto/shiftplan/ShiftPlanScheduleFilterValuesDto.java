package at.shiftcontrol.shiftservice.dto.shiftplan;

import java.time.LocalDate;
import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.lib.dto.location.LocationDto;
import at.shiftcontrol.shiftservice.dto.role.RoleDto;

@Data
@Builder
public class ShiftPlanScheduleFilterValuesDto {
    @NotNull
    @Valid
    private Collection<LocationDto> locations;

    @NotNull
    @Valid
    private Collection<RoleDto> roles;

    private LocalDate firstDate;

    private LocalDate lastDate;
}
