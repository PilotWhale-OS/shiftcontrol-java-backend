package at.shiftcontrol.shiftservice.dto;

import at.shiftcontrol.shiftservice.type.ScheduleViewType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Collection;

@Data
@Builder
public class ShiftPlanScheduleSearchDto {
    @NotNull
    private Instant startTime; // only query param required 

    // additional filter params
    private String shiftName;
    private Collection<ScheduleViewType> scheduleViewTypes;
    private Collection<String> roleNames;
    private Collection<String> locations;
    private Collection<String> tags;

    private Integer pageNumber; // pagination
    private Integer pageSize; // pagination
}
