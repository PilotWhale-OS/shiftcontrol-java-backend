package at.shiftcontrol.shiftservice.dto;

import java.time.LocalDate;
import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftPlanScheduleDto {
    private LocalDate date;
    private Collection<LocationScheduleDto> locations;
    @NotNull
    private ScheduleStatisticsDto scheduleStatistics;
}
