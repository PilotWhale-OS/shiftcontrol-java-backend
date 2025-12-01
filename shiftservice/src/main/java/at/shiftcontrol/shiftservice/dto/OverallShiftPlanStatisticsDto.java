package at.shiftcontrol.shiftservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OverallShiftPlanStatisticsDto {
    private double totalHours;
    private int totalShifts;
    private int volunteerCount;
}
