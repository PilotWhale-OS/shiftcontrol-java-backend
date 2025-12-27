package at.shiftcontrol.shiftservice.dto;

import java.time.LocalDate;
import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftPlanScheduleDto {
    @NotNull
    private LocalDate date;
    private Collection<LocationScheduleDto> locations;
    private Collection<LocationDto> allOccurringLocations;
    @NotNull
    private ScheduleStatisticsDto scheduleStatistics;
}
