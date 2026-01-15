package at.shiftcontrol.shiftservice.dto.event.schedule;

import java.time.LocalDate;
import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.location.LocationDto;
import at.shiftcontrol.shiftservice.dto.role.RoleDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventScheduleFilterValuesDto {
    @NotNull
    @Valid
    private Collection<LocationDto> locations;

    @NotNull
    @Valid
    private Collection<RoleDto> roles;

    private LocalDate firstDate;

    private LocalDate lastDate;
}
