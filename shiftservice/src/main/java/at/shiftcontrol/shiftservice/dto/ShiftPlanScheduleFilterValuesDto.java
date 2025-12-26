package at.shiftcontrol.shiftservice.dto;

import java.time.LocalDate;
import java.util.Collection;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftPlanScheduleFilterValuesDto {
    private Collection<LocationDto> locations;
    private Collection<RoleDto> roles;

    private LocalDate firstDate;
    private LocalDate lastDate;
}
