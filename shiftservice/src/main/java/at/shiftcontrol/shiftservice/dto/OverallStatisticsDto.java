package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OverallStatisticsDto {
    @NotNull
    private double totalHours;
    @NotNull
    private int totalShifts;
    @NotNull
    private int volunteerCount;
}
