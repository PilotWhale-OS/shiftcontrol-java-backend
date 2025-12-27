package at.shiftcontrol.shiftservice.dto;

import java.time.LocalDate;
import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.type.ScheduleViewType;

@Data
@Builder
public class ShiftPlanScheduleSearchDto {
    @NotNull
    private LocalDate date;
    // additional filter params
    private String shiftName;
    private ScheduleViewType scheduleViewType;
    private Collection<String> roleNames;
    private Collection<String> locations;
}
