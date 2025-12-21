package at.shiftcontrol.shiftservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OwnStatisticsDto {
    private double totalHours;
    private int totalShifts;
    private int busyDays;
}
