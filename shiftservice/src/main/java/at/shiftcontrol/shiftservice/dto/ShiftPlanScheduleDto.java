package at.shiftcontrol.shiftservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class ShiftPlanScheduleDto {
    private Collection<ShiftDto> shifts;

    // pagination info
    private long totalElements;
    private int pageNumber;
    private int pageSize;

    // schedule summary info
    private int totalShifts;
    private double totalHours;
    private int unassignedCount;
}
