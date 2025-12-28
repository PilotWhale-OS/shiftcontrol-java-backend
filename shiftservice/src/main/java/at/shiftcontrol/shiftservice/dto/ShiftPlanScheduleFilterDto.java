package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import at.shiftcontrol.shiftservice.type.ScheduleViewType;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.type.ScheduleViewType;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ShiftPlanScheduleFilterDto {
    private String shiftName;
    private ScheduleViewType scheduleViewType;
    private Collection<String> roleNames;
    private Collection<String> locations;
}
