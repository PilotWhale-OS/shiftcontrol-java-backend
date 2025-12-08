package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import lombok.Builder;
import lombok.Data;

/**
 *  Todo: This needs to be changed according to tobeh's new requirement or another DTO needs to be created
 */
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
