package at.shiftcontrol.shiftservice.dto.shiftplan;

import java.time.LocalDate;
import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.ScheduleContentDto;
import at.shiftcontrol.shiftservice.dto.ScheduleStatisticsDto;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftPlanScheduleContentDto {
    @NotNull
    private LocalDate date;

    @NotNull
    private Collection<ScheduleContentDto> scheduleContentDtos;

    @NotNull
    private ScheduleStatisticsDto scheduleStatistics;
}
