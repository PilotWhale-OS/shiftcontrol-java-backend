package at.shiftcontrol.shiftservice.dto;

import java.time.LocalDate;
import java.util.Collection;

import at.shiftcontrol.shiftservice.type.ScheduleViewType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftPlanScheduleSearchDto {
    private LocalDate date; // if not provided, all dates should be fetched
    // additional filter params
    private String shiftName;
    private Collection<ScheduleViewType> scheduleViewTypes;
    private Collection<String> roleNames;
    private Collection<String> locations;
}
