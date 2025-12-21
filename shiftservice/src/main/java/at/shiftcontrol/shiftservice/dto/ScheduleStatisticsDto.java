package at.shiftcontrol.shiftservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleStatisticsDto {
    private int totalShifts;
    private double totalHours;
    private int unassignedCount;
}
