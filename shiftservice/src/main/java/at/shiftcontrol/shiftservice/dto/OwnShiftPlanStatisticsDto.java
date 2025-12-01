package at.shiftcontrol.shiftservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OwnShiftPlanStatisticsDto {
    private int busyDays;
    private int totalShifts;
    private double totalHours;
}
