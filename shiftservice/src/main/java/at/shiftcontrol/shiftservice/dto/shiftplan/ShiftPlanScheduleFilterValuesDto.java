package at.shiftcontrol.shiftservice.dto.shiftplan;

import java.time.LocalDate;
import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.LocationDto;
import at.shiftcontrol.shiftservice.dto.role.RoleDto;
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
