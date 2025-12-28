package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import at.shiftcontrol.shiftservice.type.ScheduleViewType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ShiftPlanScheduleFilterDto {
    private String shiftName;
    private ScheduleViewType scheduleViewType;
    private Collection<String> roleNames;
    private Collection<String> locations;
}
