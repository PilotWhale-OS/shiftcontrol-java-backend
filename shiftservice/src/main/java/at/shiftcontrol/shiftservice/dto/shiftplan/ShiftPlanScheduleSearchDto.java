package at.shiftcontrol.shiftservice.dto.shiftplan;

import java.time.LocalDate;
import java.util.Collection;

import at.shiftcontrol.shiftservice.type.ScheduleViewType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

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
